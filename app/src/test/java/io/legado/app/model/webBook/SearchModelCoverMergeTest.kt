package io.legado.app.model.webBook

import io.legado.app.data.entities.SearchBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchModelCoverMergeTest {

    @Test
    fun blankCoverRepresentativeIsReplacedByCompleteCoveredSource() {
        val blank = book("https://source-a", "https://source-a/book", null)
        blank.addOrigin("https://source-old")
        val covered = book(
            "https://source-b",
            "https://source-b/book",
            "https://images.example/cover.jpg"
        )
        val list = mutableListOf(blank)

        assertTrue(mergeSameSearchBook(list, covered))

        assertSame(covered, list.single())
        assertEquals("https://source-b", list.single().origin)
        assertEquals("https://source-b/book", list.single().bookUrl)
        assertEquals("https://images.example/cover.jpg", list.single().coverUrl)
        assertEquals(
            setOf("https://source-a", "https://source-old", "https://source-b"),
            list.single().origins.toSet()
        )
    }

    @Test
    fun existingCoveredRepresentativeIsKeptAndOriginsAreMerged() {
        val covered = book(
            "https://source-a",
            "https://source-a/book",
            "https://images.example/a.jpg"
        )
        val incoming = book(
            "https://source-b",
            "https://source-b/book",
            "https://images.example/b.jpg"
        )
        incoming.addOrigin("https://source-c")
        val list = mutableListOf(covered)

        assertTrue(mergeSameSearchBook(list, incoming))

        assertSame(covered, list.single())
        assertEquals("https://images.example/a.jpg", list.single().coverUrl)
        assertEquals(
            setOf("https://source-a", "https://source-b", "https://source-c"),
            list.single().origins.toSet()
        )
    }

    private fun book(origin: String, bookUrl: String, coverUrl: String?) = SearchBook(
        name = "腐朽世界",
        author = "滚开",
        origin = origin,
        originName = origin,
        bookUrl = bookUrl,
        coverUrl = coverUrl,
    )
}
