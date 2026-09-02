package io.legado.app.ui.book.read

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanReaderMenuContractTest {

    @Test
    fun readerMenuExposesProgressAndHighFrequencyActions() {
        val layout = source("app/src/main/res/layout/view_read_menu.xml")
        listOf(
            "@+id/tv_read_progress",
            "@+id/tv_preload",
            "@+id/ll_bookmark",
            "@+id/ll_cache",
            "@+id/ll_change_source",
            "@drawable/xuanjuan_reader_action_bg",
            "@string/xuanjuan_reader_progress",
            "@string/bookmark",
            "@string/offline_cache",
            "@string/xuanjuan_reader_change_source",
            "@string/xuanjuan_reader_preload_status",
        ).forEach { assertTrue("missing $it", layout.contains(it)) }
    }

    @Test
    fun readerQuickActionsReuseExistingReaderBehavior() {
        val menu = source("app/src/main/java/io/legado/app/ui/book/read/ReadMenu.kt")
        val activity = source("app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt")

        assertTrue(menu.contains("callBack.addBookmark()"))
        assertTrue(menu.contains("callBack.openOfflineCache()"))
        assertTrue(menu.contains("callBack.openBookChangeSource()"))
        assertTrue(menu.contains("fun addBookmark()"))
        assertTrue(menu.contains("fun openOfflineCache()"))
        assertTrue(menu.contains("fun openBookChangeSource()"))
        assertTrue(activity.contains("override fun openOfflineCache()"))
        assertTrue(activity.contains("showDownloadDialog()"))
        assertTrue(activity.contains("override fun openBookChangeSource()"))
        assertTrue(activity.contains("showBookChangeSource()"))
    }

    @Test
    fun readerProgressUsesCurrentAndTotalChapterState() {
        val menu = source("app/src/main/java/io/legado/app/ui/book/read/ReadMenu.kt")
        assertTrue(menu.contains("ReadBook.simulatedChapterSize"))
        assertTrue(menu.contains("ReadBook.durChapterIndex + 1"))
        assertTrue(menu.contains("R.string.xuanjuan_reader_progress"))
    }

    @Test
    fun readerPreloadControlReusesExistingPreDownloadPipeline() {
        val menu = source("app/src/main/java/io/legado/app/ui/book/read/ReadMenu.kt")
        val activity = source("app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt")
        val readBook = source("app/src/main/java/io/legado/app/model/ReadBook.kt")
        val dialog = source("app/src/main/res/layout/dialog_xuanjuan_preload.xml")

        assertTrue(menu.contains("AppConfig.preDownloadNum"))
        assertTrue(menu.contains("callBack.showPreloadSelector()"))
        assertTrue(menu.contains("fun showPreloadSelector()"))
        assertTrue(activity.contains("override fun showPreloadSelector()"))
        assertTrue(activity.contains("AppConfig.preDownloadNum = index.coerceIn(0, 3)"))
        assertTrue(activity.contains("ReadBook.applyPreDownloadConfig()"))
        assertTrue(activity.contains("DialogXuanjuanPreloadBinding"))
        assertTrue(activity.contains("setBackgroundDrawableResource(android.R.color.transparent)"))
        assertTrue(activity.contains("dimAmount = 0.28f"))
        assertTrue(!activity.contains("selector(R.string.xuanjuan_reader_preload_title"))
        assertTrue(dialog.contains("@color/xuanjuan_gold_soft"))
        assertTrue(dialog.contains("@color/xuanjuan_text_primary"))
        assertTrue(dialog.contains("@drawable/xuanjuan_reader_preload_option_bg"))
        assertTrue(readBook.contains("fun applyPreDownloadConfig()"))
        assertTrue(readBook.contains("preDownloadTask?.cancel()"))
    }

    private fun source(path: String): String {
        var current = File(System.getProperty("user.dir") ?: ".").canonicalFile
        repeat(8) {
            val candidate = File(current, path)
            if (candidate.isFile) return candidate.readText()
            current = current.parentFile ?: return@repeat
        }
        error("Project file not found: $path")
    }
}
