package io.legado.app.ui.book.read

import io.legado.app.data.entities.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XuanjuanReaderCacheTest {

    @Test
    fun next20StartsAfterCurrentAndCapsAtTwentyChapters() {
        val book = book(current = 10, total = 100)

        assertEquals(
            XuanjuanReaderCacheRange(11, 30),
            book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.NEXT_20),
        )
    }

    @Test
    fun next50ClampsAtBookEnd() {
        val book = book(current = 80, total = 100)

        assertEquals(
            XuanjuanReaderCacheRange(81, 99),
            book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.NEXT_50),
        )
    }

    @Test
    fun unreadCachesEverythingAfterCurrentChapter() {
        val book = book(current = 4, total = 12)

        assertEquals(
            XuanjuanReaderCacheRange(5, 11),
            book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.UNREAD),
        )
    }

    @Test
    fun fullBookAlwaysStartsAtFirstChapter() {
        val book = book(current = 8, total = 12)

        assertEquals(
            XuanjuanReaderCacheRange(0, 11),
            book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.ALL),
        )
    }

    @Test
    fun nextAndUnreadAreEmptyAtLastChapter() {
        val book = book(current = 11, total = 12)

        assertNull(book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.NEXT_20))
        assertNull(book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.NEXT_50))
        assertNull(book.xuanjuanReaderCacheRange(XuanjuanReaderCachePreset.UNREAD))
    }

    private fun book(current: Int, total: Int) = Book(
        bookUrl = "https://example.com/book",
        origin = "https://example.com",
        name = "测试书",
        author = "作者",
        durChapterIndex = current,
        totalChapterNum = total,
    )
}
