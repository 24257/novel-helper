package io.legado.app.model.webBook

import androidx.test.platform.app.InstrumentationRegistry
import io.legado.app.data.entities.BookSource
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URI

class CommunityLiveProbeInstrumentedTest {

    @Test
    fun liveProbe() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(
            "manual live probe; pass strictLiveProbe=1 to run",
            arguments.getString("strictLiveProbe") == "1",
        )
        val candidateAsset = arguments.getString("strictCandidateAsset")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val assets = if (candidateAsset == null) {
            instrumentation.targetContext.assets
        } else {
            instrumentation.context.assets
        }
        val json = assets
            .open(candidateAsset ?: "defaultData/bookSources/community_strict7.json")
            .bufferedReader()
            .use { it.readText() }
        val parsedSources = GSON.fromJsonArray<BookSource>(json).getOrThrow()
        val template14oz by lazy {
            val templateJson = instrumentation.targetContext.assets
                .open("defaultData/bookSources/community_cover.json")
                .bufferedReader()
                .use { it.readText() }
            GSON.fromJsonArray<BookSource>(templateJson).getOrThrow()
                .first { it.bookSourceUrl == "https://www.14oz.net/" }
        }
        val allSources = parsedSources.map { source ->
            if (source.bookSourceComment == "TEST_CLONE_14OZ") {
                template14oz.copy(
                    bookSourceName = source.bookSourceName,
                    bookSourceGroup = source.bookSourceGroup,
                    bookSourceUrl = source.bookSourceUrl,
                    enabled = true,
                    enabledExplore = false,
                    bookSourceComment = source.bookSourceComment,
                )
            } else {
                source
            }
        }
        if (candidateAsset == null) {
            assertEquals(7, allSources.size)
        }
        val requested = arguments
            .getString("strictSource")
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val sources = if (requested.isEmpty()) {
            allSources
        } else {
            allSources.filter { source ->
                requested.any { token ->
                    source.bookSourceUrl.contains(token, ignoreCase = true) ||
                        source.bookSourceName.contains(token, ignoreCase = true)
                }
            }.also {
                check(it.isNotEmpty()) { "strictSource did not match any bundled source: $requested" }
            }
        }

        println("PROBE_CANDIDATES ${sources.size}")
        val passed = mutableListOf<String>()
        for (source in sources) {
            try {
                withTimeout(75_000) {
                    println("PROBE_STAGE ${source.bookSourceName}|search")
                    val results = WebBook.searchBookAwait(source, "腐朽世界", 1)
                    val exact = results.firstOrNull {
                        it.name.trim() == "腐朽世界" && it.author.contains("滚开")
                    } ?: error(
                        "exact search result missing; results=" +
                            results.take(3).map { it.name + "/" + it.author }
                    )
                    val book = exact.toBook()
                    println("PROBE_STAGE ${source.bookSourceName}|detail")
                    WebBook.getBookInfoAwait(source, book, canReName = false)
                    check(book.name.trim() == "腐朽世界") { "detail title=${book.name}" }
                    check(book.author.contains("滚开")) { "detail author=${book.author}" }

                    val cover = (book.coverUrl?.takeIf { it.isNotBlank() } ?: exact.coverUrl)
                        ?.substringBefore(",{")
                        ?.trim()
                        .orEmpty()
                    check(cover.isNotBlank()) { "cover empty" }
                    val lower = cover.lowercase()
                    check(listOf("nocover", "no-cover", "default", "logo", "placeholder").none(lower::contains)) {
                        "placeholder cover=$cover"
                    }
                    println("PROBE_STAGE ${source.bookSourceName}|cover|$cover")
                    val coverBytes = checkCover(resolve(book.bookUrl, cover), book.bookUrl)

                    println("PROBE_STAGE ${source.bookSourceName}|toc")
                    val chapters = WebBook.getChapterListAwait(source, book).getOrThrow()
                        .filterNot {
                            it.isVolume || it.title.contains("简介") || it.title.contains("内容介绍")
                        }
                    check(chapters.size >= 500) { "chapters=${chapters.size}" }
                    val first = chapters.first()
                    val late = chapters[(chapters.size * 3 / 4).coerceAtMost(chapters.lastIndex)]
                    println("PROBE_STAGE ${source.bookSourceName}|content-first|${first.title}")
                    val firstText = WebBook.getContentAwait(source, book, first, needSave = false)
                    println("PROBE_STAGE ${source.bookSourceName}|content-late|${late.title}")
                    val lateText = WebBook.getContentAwait(source, book, late, needSave = false)
                    val firstLen = textLen(firstText)
                    val lateLen = textLen(lateText)
                    check(firstLen >= 500) { "first content len=$firstLen title=${first.title}" }
                    check(lateLen >= 500) { "late content len=$lateLen title=${late.title}" }

                    println(
                        "PROBE_PASS ${source.bookSourceName}|${source.bookSourceUrl}|" +
                            "coverBytes=$coverBytes|chapters=${chapters.size}|" +
                            "first=${first.title}:$firstLen|late=${late.title}:$lateLen|cover=$cover"
                    )
                    passed += source.bookSourceUrl
                }
            } catch (e: Throwable) {
                println(
                    "PROBE_FAIL ${source.bookSourceName}|${source.bookSourceUrl}|" +
                        "${e::class.java.simpleName}:${e.message}"
                )
            }
        }

        println("PROBE_TOTAL passed=${passed.size} urls=$passed")
        assertEquals("all strict bundled sources must pass live acceptance", sources.size, passed.size)
    }

    private fun clean(url: String) = url.substringBefore("##").substringBefore("#").trimEnd('/')

    private fun resolve(base: String, value: String): String =
        runCatching { URI(clean(base) + "/").resolve(value).toString() }.getOrDefault(value)

    private fun checkCover(url: String, referer: String): Int {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/136.0 Mobile Safari/537.36",
        )
        connection.setRequestProperty("Referer", referer)
        try {
            val code = connection.responseCode
            check(code in 200..299) { "cover http=$code url=$url" }
            val type = connection.contentType.orEmpty().lowercase()
            check(type.startsWith("image/")) { "cover type=$type url=$url" }
            val bytes = connection.inputStream.use { input ->
                val buffer = ByteArray(8192)
                var total = 0
                while (total <= 1_048_576) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                }
                total
            }
            check(bytes > 2048) { "cover too small=$bytes url=$url" }
            return bytes
        } finally {
            connection.disconnect()
        }
    }

    private fun textLen(html: String): Int = html
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("&[a-zA-Z#0-9]+;"), " ")
        .replace(Regex("\\s+"), "")
        .length
}
