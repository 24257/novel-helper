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
    fun searchHelpUsesDedicatedHistoryCard() {
        val layout = projectFile("src/main/res/layout/activity_book_search.xml").readText()
        val panel = projectFile(
            "src/main/res/drawable/novel_helper_search_history_panel.xml",
        ).readText()

        assertTrue(layout.contains("@+id/input_help_panel"))
        assertTrue(layout.contains("@drawable/novel_helper_search_history_panel"))
        assertTrue(panel.contains("android:radius=\"20dp\""))
        assertTrue(panel.contains("@color/novel_helper_card_stroke"))
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
