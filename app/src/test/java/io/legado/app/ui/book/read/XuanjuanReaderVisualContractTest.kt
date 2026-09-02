package io.legado.app.ui.book.read

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanReaderVisualContractTest {

    @Test
    fun readerMenuUsesFixedHighContrastXuanjuanPalette() {
        val source = read("src/main/java/io/legado/app/ui/book/read/ReadMenu.kt")
        val layout = read("src/main/res/layout/view_read_menu.xml")

        assertTrue(source.contains("R.color.xuanjuan_surface_high"))
        assertTrue(source.contains("R.color.xuanjuan_text_primary"))
        assertTrue(source.contains("R.color.xuanjuan_text_secondary"))
        assertTrue(source.contains("setStroke("))
        assertTrue(layout.contains("@color/xuanjuan_text_primary"))
        assertTrue(layout.contains("@color/xuanjuan_text_secondary"))
    }

    @Test
    fun readerMenuDoesNotAutoOpenHelpDialog() {
        val source = read("src/main/java/io/legado/app/ui/book/read/ReadMenu.kt")

        assertTrue(!source.contains("readMenuHelpVersionIsLast"))
        assertTrue(!source.contains("LocalConfig.readMenuHelpVersionIsLast"))
    }

    @Test
    fun downloadDialogHasReaderOnlyQuickActions() {
        val layout = read("src/main/res/layout/dialog_xuanjuan_download_choice.xml")
        val activity = read("src/main/java/io/legado/app/ui/book/read/BaseReadBookActivity.kt")

        assertTrue(layout.contains("@+id/reader_quick_cache"))
        assertTrue(layout.contains("@+id/tv_reader_cache_title"))
        assertTrue(layout.contains("@color/xuanjuan_gold_soft"))
        assertTrue(layout.contains("@+id/cache_next_20"))
        assertTrue(layout.contains("@+id/cache_next_50"))
        assertTrue(layout.contains("@+id/cache_unread"))
        assertTrue(layout.contains("@+id/cache_all"))
        assertTrue(activity.contains("DialogXuanjuanDownloadChoiceBinding"))
        assertTrue(activity.contains("alert(title = null as CharSequence?)"))
        assertTrue(activity.contains("applyPreset(XuanjuanReaderCachePreset.NEXT_20)"))
    }

    private fun read(path: String): String = sequenceOf(File(path), File("app/$path"))
        .first(File::isFile)
        .readText()
}
