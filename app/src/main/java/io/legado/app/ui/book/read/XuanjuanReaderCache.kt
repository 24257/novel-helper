package io.legado.app.ui.book.read

import io.legado.app.data.entities.Book
import kotlin.math.min

internal enum class XuanjuanReaderCachePreset {
    NEXT_20,
    NEXT_50,
    UNREAD,
    ALL,
}

internal data class XuanjuanReaderCacheRange(
    val startIndex: Int,
    val endIndex: Int,
)

internal fun Book.xuanjuanReaderCacheRange(
    preset: XuanjuanReaderCachePreset,
): XuanjuanReaderCacheRange? {
    val lastIndex = totalChapterNum - 1
    if (lastIndex < 0) return null

    return when (preset) {
        XuanjuanReaderCachePreset.ALL -> XuanjuanReaderCacheRange(0, lastIndex)
        XuanjuanReaderCachePreset.NEXT_20 -> nextCacheRange(lastIndex, 20)
        XuanjuanReaderCachePreset.NEXT_50 -> nextCacheRange(lastIndex, 50)
        XuanjuanReaderCachePreset.UNREAD -> nextCacheRange(lastIndex, Int.MAX_VALUE)
    }
}

private fun Book.nextCacheRange(
    lastIndex: Int,
    count: Int,
): XuanjuanReaderCacheRange? {
    val startIndex = durChapterIndex + 1
    if (startIndex > lastIndex) return null
    val endIndex = if (count == Int.MAX_VALUE) {
        lastIndex
    } else {
        min(lastIndex, durChapterIndex + count)
    }
    return XuanjuanReaderCacheRange(startIndex, endIndex)
}
