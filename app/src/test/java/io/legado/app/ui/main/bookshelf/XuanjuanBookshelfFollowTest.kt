package io.legado.app.ui.main.bookshelf

import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XuanjuanBookshelfFollowTest {

    @Test
    fun staleOnlineBookIsEligibleButFreshBookIsThrottled() {
        val now = 10_000_000L
        val stale = onlineBook(lastCheckTime = now - XUANJUAN_AUTO_FOLLOW_INTERVAL_MS)
        val fresh = onlineBook(lastCheckTime = now - XUANJUAN_AUTO_FOLLOW_INTERVAL_MS + 1)
        assertTrue(stale.shouldXuanjuanAutoFollow(now))
        assertFalse(fresh.shouldXuanjuanAutoFollow(now))
    }

    @Test
    fun localOrNonUpdatingBookNeverAutoFollows() {
        val now = 10_000_000L
        val local = onlineBook(0L).copy(type = BookType.local)
        val disabled = onlineBook(0L).copy(canUpdate = false)
        assertFalse(local.shouldXuanjuanAutoFollow(now))
        assertFalse(disabled.shouldXuanjuanAutoFollow(now))
    }

    @Test
    fun checkingAndFailureHaveHighestDisplayPriority() {
        val now = 10_000_000L
        val failed = onlineBook(now).copy(
            type = BookType.text or BookType.updateError,
            lastCheckCount = 7,
            totalChapterNum = 20,
            durChapterIndex = 2,
        )
        assertEquals(
            XuanjuanBookshelfFollowState.CHECKING,
            failed.xuanjuanBookshelfFollowStatus(updating = true, now = now).state,
        )
        assertEquals(
            XuanjuanBookshelfFollowState.FAILED,
            failed.xuanjuanBookshelfFollowStatus(updating = false, now = now).state,
        )
    }

    @Test
    fun newlyFoundChaptersUseLastCheckCount() {
        val now = 10_000_000L
        val book = onlineBook(now).copy(lastCheckCount = 4)
        val status = book.xuanjuanBookshelfFollowStatus(updating = false, now = now)
        assertEquals(XuanjuanBookshelfFollowState.UPDATED, status.state)
        assertEquals(4, status.count)
    }

    @Test
    fun recentlyCheckedFullyReadBookShowsCurrent() {
        val now = 10_000_000L
        val book = onlineBook(now - 1_000L).copy(
            totalChapterNum = 10,
            durChapterIndex = 9,
            lastCheckCount = 0,
        )
        assertEquals(
            XuanjuanBookshelfFollowState.CURRENT,
            book.xuanjuanBookshelfFollowStatus(updating = false, now = now).state,
        )
    }

    private fun onlineBook(lastCheckTime: Long): Book {
        return Book(
            bookUrl = "https://example.com/book",
            origin = "https://example.com",
            type = BookType.text,
            canUpdate = true,
            lastCheckTime = lastCheckTime,
        )
    }
}
