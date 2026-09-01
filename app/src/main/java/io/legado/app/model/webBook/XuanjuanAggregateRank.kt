package io.legado.app.model.webBook

import io.legado.app.constant.AppLog
import io.legado.app.data.appDb
import io.legado.app.data.entities.SearchBook
import io.legado.app.help.source.exploreKinds
import io.legado.app.utils.GSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

const val XUANJUAN_AGGREGATE_SOURCE_URL = "xuanjuan://aggregate"
const val XUANJUAN_AGGREGATE_HOT_URL = "xuanjuan://aggregate/hot"
internal const val XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY = "__xuanjuan_aggregate_sources"
internal const val XUANJUAN_AGGREGATE_SOURCE_NAMES_KEY = "__xuanjuan_aggregate_source_names"

fun isXuanjuanAggregateRequest(sourceUrl: String?, exploreUrl: String?): Boolean {
    return sourceUrl == XUANJUAN_AGGREGATE_SOURCE_URL &&
        exploreUrl == XUANJUAN_AGGREGATE_HOT_URL
}

internal data class AggregateRankCandidate(
    val book: SearchBook,
    val rank: Int,
    val sourceWeight: Int = 0,
)

private data class AggregateRankedBook(
    val book: SearchBook,
    val sourceCount: Int,
    val score: Int,
    val bestRank: Int,
)

object XuanjuanAggregateRank {

    suspend fun load(page: Int): List<SearchBook> {
        if (page != 1) return emptyList()
        val sources = withContext(Dispatchers.IO) {
            appDb.bookSourceDao.allEnabledExplore
                .filter { !it.exploreUrl.isNullOrBlank() }
        }
        val candidates = supervisorScope {
            sources.mapNotNull { source ->
                val hotKind = runCatching { source.exploreKinds() }
                    .onFailure {
                        AppLog.put("玄卷聚合榜解析榜单入口失败: ${source.bookSourceName}", it)
                    }
                    .getOrDefault(emptyList())
                    .firstOrNull { kind ->
                        val url = kind.url
                        !url.isNullOrBlank() && isHotRankingTitle(kind.title)
                    } ?: return@mapNotNull null
                val url = hotKind.url ?: return@mapNotNull null
                async(Dispatchers.IO) {
                    val books = withTimeoutOrNull(20_000L) {
                        runCatching {
                            WebBook.exploreBookAwait(source, url, 1)
                        }.onFailure {
                            AppLog.put("玄卷聚合榜加载失败: ${source.bookSourceName}", it)
                        }.getOrDefault(arrayListOf())
                    } ?: run {
                        AppLog.put("玄卷聚合榜加载超时: ${source.bookSourceName}")
                        emptyList()
                    }
                    books.take(60).mapIndexed { index, book ->
                        AggregateRankCandidate(
                            book = book,
                            rank = index + 1,
                            sourceWeight = source.weight,
                        )
                    }
                }
            }.awaitAll().flatten()
        }
        return aggregateRankBooks(candidates, 80)
    }
}

internal fun isHotRankingTitle(title: String): Boolean {
    val value = title.lowercase(Locale.ROOT).replace(" ", "")
    if (value.isBlank()) return false
    val reliableExcluded = listOf(
        "\u5b8c\u672c", "\u5168\u672c", "\u6700\u65b0", "\u7384\u5e7b",
        "\u4ed9\u4fa0", "\u90fd\u5e02", "\u5386\u53f2", "\u79d1\u5e7b",
        "\u8a00\u60c5", "\u519b\u4e8b", "\u6b66\u4fa0", "\u6e38\u620f",
        "\u4f53\u80b2", "\u60ac\u7591", "\u7075\u5f02", "\u6821\u56ed",
        "\u9752\u6625", "\u5206\u7c7b",
    )
    if (reliableExcluded.any(value::contains)) return false
    val reliableHot = listOf(
        "\u70ed\u95e8", "\u6392\u884c", "\u70ed\u699c", "\u4eba\u6c14",
        "\u7545\u9500", "\u63a8\u8350", "\u699c", "top",
    )
    if (reliableHot.any(value::contains)) return true
    val excluded = listOf(
        "完本", "全本", "最新", "更新", "玄幻", "仙侠", "都市", "历史", "科幻",
        "武侠", "言情", "军事", "悬疑", "灵异", "竞技", "网游", "分类", "书库"
    )
    if (excluded.any(value::contains)) return false
    return listOf("热门", "排行", "热榜", "人气", "倾心", "推荐", "榜单", "top")
        .any(value::contains)
}

internal fun normalizeAggregateText(value: String): String {
    return value.lowercase(Locale.ROOT)
        .replace(Regex("[\\s\\p{P}\\p{S}]+"), "")
}

internal fun aggregateRankBooks(
    candidates: List<AggregateRankCandidate>,
    limit: Int = 80,
): List<SearchBook> {
    if (candidates.isEmpty()) return emptyList()
    val ranked = arrayListOf<AggregateRankedBook>()
    candidates
        .filter { it.book.name.isNotBlank() && it.book.origin.isNotBlank() }
        .groupBy { normalizeAggregateText(it.book.name) }
        .filterKeys { it.isNotBlank() }
        .values
        .forEach { sameName ->
            val knownAuthors = sameName
                .map { normalizeAggregateText(it.book.author) }
                .filter { it.isNotBlank() }
                .distinct()
            if (knownAuthors.size <= 1) {
                ranked.add(buildAggregateBook(sameName))
            } else {
                sameName
                    .filter { normalizeAggregateText(it.book.author).isNotBlank() }
                    .groupBy { normalizeAggregateText(it.book.author) }
                    .values
                    .forEach { ranked.add(buildAggregateBook(it)) }
                val unknownAuthor = sameName.filter {
                    normalizeAggregateText(it.book.author).isBlank()
                }
                if (unknownAuthor.isNotEmpty()) {
                    ranked.add(buildAggregateBook(unknownAuthor))
                }
            }
        }
    return ranked
        .sortedWith(
            compareByDescending<AggregateRankedBook> { it.sourceCount }
                .thenByDescending { it.score }
                .thenBy { it.bestRank }
                .thenBy { normalizeAggregateText(it.book.name) }
        )
        .take(limit.coerceAtLeast(0))
        .map { it.book }
}

private fun buildAggregateBook(group: List<AggregateRankCandidate>): AggregateRankedBook {
    val bySource = group.groupBy { it.book.origin }.values.map { sourceItems ->
        sourceItems.minWithOrNull(
            compareBy<AggregateRankCandidate> { it.rank }
                .thenByDescending { informationRichness(it.book) }
        ) ?: sourceItems.first()
    }
    val sourceCount = bySource.size
    val score = bySource.sumOf { candidate ->
        val rankPoints = (121 - candidate.rank.coerceIn(1, 120)).coerceAtLeast(1)
        rankPoints * 10 + candidate.sourceWeight.coerceIn(-20, 20)
    }
    val bestRank = bySource.minOfOrNull { it.rank } ?: Int.MAX_VALUE
    val representative = bySource.maxWithOrNull(
        compareBy<AggregateRankCandidate> { informationRichness(it.book) }
            .thenBy { -it.rank }
            .thenBy { it.sourceWeight.coerceIn(-20, 20) }
    ) ?: group.first()
    val variables = representative.book.variableMap.toMutableMap().apply {
        put(XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY, sourceCount.toString())
        put(
            XUANJUAN_AGGREGATE_SOURCE_NAMES_KEY,
            bySource.map { it.book.originName.ifBlank { it.book.origin } }
                .distinct()
                .joinToString("|")
        )
    }
    return AggregateRankedBook(
        book = representative.book.copy(variable = GSON.toJson(variables)),
        sourceCount = sourceCount,
        score = score,
        bestRank = bestRank,
    )
}

private fun informationRichness(book: SearchBook): Int {
    var score = 0
    if (!book.coverUrl.isNullOrBlank()) score += 4
    if (!book.intro.isNullOrBlank()) score += 3
    if (!book.latestChapterTitle.isNullOrBlank()) score += 2
    if (!book.author.isBlank()) score += 1
    if (!book.kind.isNullOrBlank()) score += 1
    return score
}
