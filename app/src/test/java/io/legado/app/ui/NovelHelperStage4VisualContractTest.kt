package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage4VisualContractTest {

    @Test
    fun mainNavigationUsesFloatingNovelHelperDock() {
        val layout = projectFile("src/main/res/layout/activity_main.xml").readText()
        val dock = projectFile("src/main/res/drawable/novel_helper_bottom_bar.xml").readText()
        val view = projectFile(
            "src/main/java/io/legado/app/lib/theme/view/ThemeBottomNavigationVIew.kt",
        ).readText()

        assertTrue(layout.contains("android:layout_marginStart=\"12dp\""))
        assertTrue(layout.contains("android:layout_marginBottom=\"8dp\""))
        assertTrue(layout.contains("app:itemRippleColor=\"@color/novel_helper_focus_overlay\""))
        assertTrue(dock.contains("android:radius=\"24dp\""))
        assertTrue(view.contains("elevation = 8.dpToPx().toFloat()"))
    }

    @Test
    fun searchHelpUsesFlatHistorySurfaceAndXuanjuanChips() {
        val layout = projectFile("src/main/res/layout/activity_book_search.xml").readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/search/HistoryKeyAdapter.kt",
        ).readText()

        assertTrue(layout.contains("@+id/input_help_panel"))
        assertTrue(layout.contains("android:background=\"@android:color/transparent\""))
        assertTrue(layout.contains("@+id/rv_history_key"))
        assertTrue(layout.contains("android:clipToPadding=\"false\""))
        assertTrue(adapter.contains("R.drawable.xuanjuan_explore_chip"))
        assertTrue(adapter.contains("R.color.xuanjuan_text_primary"))
    }

    @Test
    fun bookshelfHeaderUsesCardAndChinesePreviewCopy() {
        val header = projectFile("src/main/res/layout/view_bookshelf_header.xml").readText()

        assertTrue(header.contains("@drawable/novel_helper_preference_card"))
        assertTrue(header.contains("128 本 · 23 本阅读中"))
        assertTrue(header.contains("某本小说"))
        assertTrue(header.contains("第 60 章"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
