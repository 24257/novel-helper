package io.legado.app.model.webBook

import io.legado.app.data.entities.SearchBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XuanjuanAggregateRankTest {

    @Test
    fun hotRankingTitleRecognizesChineseRankingNames() {
        assertTrue(isHotRankingTitle("热门榜"))
        assertTrue(isHotRankingTitle("人气排行"))
        assertTrue(isHotRankingTitle("TOP 100"))
        assertFalse(isHotRankingTitle("完本榜"))
        assertFalse(isHotRankingTitle("最新更新"))
        assertFalse(isHotRankingTitle("玄幻"))
    }

    @Test
    fun multiSourceConsensusOutranksSingleSourceNumberOne() {
        val result = aggregateRankBooks(
            listOf(
                candidate("单站榜首", "甲", "a", 1),
                candidate("共同热门", "乙", "a", 30),
                candidate("共同热门", "乙", "b", 40),
                candidate("共同热门", "乙", "c", 50),
            )
        )
        assertEquals("共同热门", result.first().name)
        assertEquals("3", result.first().variableMap[XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY])
    }

    @Test
    fun normalizationMergesWhitespacePunctuationAndCase() {
        val result = aggregateRankBooks(
            listOf(
                candidate("The Book！", "Alice", "a", 2),
                candidate(" the-book ", "ALICE", "b", 8),
            )
        )
        assertEquals(1, result.size)
        assertEquals("2", result.first().variableMap[XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY])
    }

    @Test
    fun sameTitleDifferentKnownAuthorsStaySeparate() {
        val result = aggregateRankBooks(
            listOf(
                candidate("同名书", "作者甲", "a", 1),
                candidate("同名书", "作者乙", "b", 1),
            )
        )
        assertEquals(2, result.size)
        assertNotEquals(result[0].author, result[1].author)
    }

    @Test
    fun missingAuthorMergesWhenThereIsOnlyOneKnownAuthor() {
        val result = aggregateRankBooks(
            listOf(
                candidate("唯一作者书", "作者甲", "a", 6),
                candidate("唯一作者书", "", "b", 5),
            )
        )
        assertEquals(1, result.size)
        assertEquals("2", result.first().variableMap[XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY])
    }

    @Test
    fun representativeKeepsRealSourceAndOriginalVariables() {
        val rich = SearchBook(
            name = "保留来源",
            author = "作者",
            origin = "https://source-b.example",
            originName = "来源B",
            bookUrl = "https://source-b.example/book/2",
            tocUrl = "https://source-b.example/book/2",
            coverUrl = "https://source-b.example/cover.jpg",
            intro = "介绍",
            latestChapterTitle = "最新章",
            variable = "{\"token\":\"keep-me\"}",
        )
        val result = aggregateRankBooks(
            listOf(
                candidate("保留来源", "作者", "a", 1),
                AggregateRankCandidate(rich, rank = 10, sourceWeight = 0),
            )
        ).single()
        assertEquals("https://source-b.example", result.origin)
        assertEquals("https://source-b.example/book/2", result.bookUrl)
        assertEquals("keep-me", result.variableMap["token"])
        assertEquals("2", result.variableMap[XUANJUAN_AGGREGATE_SOURCE_COUNT_KEY])
        assertTrue(result.variableMap[XUANJUAN_AGGREGATE_SOURCE_NAMES_KEY]!!.contains("来源B"))
    }

    private fun candidate(
        name: String,
        author: String,
        source: String,
        rank: Int,
    ): AggregateRankCandidate {
        val origin = "https://$source.example"
        return AggregateRankCandidate(
            SearchBook(
                name = name,
                author = author,
                origin = origin,
                originName = "来源$source",
                bookUrl = "$origin/book/$rank",
                tocUrl = "$origin/book/$rank",
            ),
            rank = rank,
            sourceWeight = 0,
        )
    }
}
