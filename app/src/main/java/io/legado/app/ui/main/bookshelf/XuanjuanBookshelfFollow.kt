package io.legado.app.ui.main.bookshelf

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.data.entities.Book
import io.legado.app.help.book.isLocal
import io.legado.app.help.book.isNotShelf
import io.legado.app.help.book.isUpError
import io.legado.app.help.config.AppConfig
import io.legado.app.ui.widget.text.BadgeView
import io.legado.app.utils.gone
import io.legado.app.utils.invisible
import io.legado.app.utils.visible

internal const val XUANJUAN_AUTO_FOLLOW_INTERVAL_MS = 30 * 60 * 1000L
internal const val XUANJUAN_AUTO_FOLLOW_SESSION_GUARD_MS = 60 * 1000L

internal enum class XuanjuanBookshelfFollowState {
    CHECKING,
    FAILED,
    UPDATED,
    UNREAD,
    CURRENT,
    IDLE,
}

internal data class XuanjuanBookshelfFollowStatus(
    val state: XuanjuanBookshelfFollowState,
    val count: Int = 0,
)

internal fun Book.shouldXuanjuanAutoFollow(now: Long): Boolean {
    if (isLocal || isNotShelf || !canUpdate) return false
    if (lastCheckTime <= 0L) return true
    return now - lastCheckTime >= XUANJUAN_AUTO_FOLLOW_INTERVAL_MS
}

internal fun Book.xuanjuanBookshelfFollowStatus(
    updating: Boolean,
    now: Long = System.currentTimeMillis(),
): XuanjuanBookshelfFollowStatus {
    if (isLocal) return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.IDLE)
    if (updating) return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.CHECKING)
    if (isUpError) return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.FAILED)
    if (lastCheckCount > 0) {
        return XuanjuanBookshelfFollowStatus(
            XuanjuanBookshelfFollowState.UPDATED,
            lastCheckCount,
        )
    }
    val unread = getUnreadChapterNum()
    if (unread > 0) {
        return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.UNREAD, unread)
    }
    if (lastCheckTime > 0L && now >= lastCheckTime &&
        now - lastCheckTime < XUANJUAN_AUTO_FOLLOW_INTERVAL_MS
    ) {
        return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.CURRENT)
    }
    return XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.IDLE)
}

internal fun bindXuanjuanBookshelfFollowStatus(
    context: Context,
    book: Book,
    updating: Boolean,
    statusView: TextView,
    badgeView: BadgeView,
    loadingView: android.view.View,
) {
    when (val status = book.xuanjuanBookshelfFollowStatus(updating)) {
        XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.CHECKING) -> {
            statusView.gone()
            badgeView.invisible()
            loadingView.visible()
        }
        XuanjuanBookshelfFollowStatus(XuanjuanBookshelfFollowState.FAILED) -> {
            loadingView.gone()
            badgeView.invisible()
            statusView.text = context.getString(R.string.xuanjuan_bookshelf_update_failed)
            statusView.setTextColor(ContextCompat.getColor(context, R.color.error))
            statusView.visible()
        }
        else -> {
            loadingView.gone()
            statusView.setTextColor(ContextCompat.getColor(context, R.color.xuanjuan_gold_soft))
            when (status.state) {
                XuanjuanBookshelfFollowState.UPDATED -> {
                    badgeView.invisible()
                    statusView.text = context.getString(
                        R.string.xuanjuan_bookshelf_updated_count,
                        status.count,
                    )
                    statusView.visible()
                }
                XuanjuanBookshelfFollowState.UNREAD -> {
                    statusView.gone()
                    if (AppConfig.showUnread) {
                        badgeView.setHighlight(book.lastCheckCount > 0)
                        badgeView.setBadgeCount(status.count)
                        badgeView.visible()
                    } else {
                        badgeView.invisible()
                    }
                }
                XuanjuanBookshelfFollowState.CURRENT -> {
                    badgeView.invisible()
                    statusView.setText(R.string.xuanjuan_bookshelf_current)
                    statusView.visible()
                }
                else -> {
                    statusView.gone()
                    if (AppConfig.showUnread) {
                        badgeView.setHighlight(false)
                        badgeView.setBadgeCount(0)
                    }
                    badgeView.invisible()
                }
            }
        }
    }
}
