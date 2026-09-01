package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage7VisualContractTest {

    @Test
    fun utilityPagesUseUnifiedBackgroundAndStateCards() {
        listOf(
            "activity_file_manage.xml",
            "activity_import_book.xml",
            "activity_rule_sub.xml",
            "activity_highlight_rule.xml",
        ).forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue(layout.contains("android:background=\"@color/background\""))
            assertTrue(layout.contains("@drawable/novel_helper_state_card"))
            assertTrue(layout.contains("android:layout_width=\"280dp\""))
            assertTrue(layout.contains("android:padding=\"20dp\""))
        }
    }

    @Test
    fun fileAndImportPagesUseCardPathSurfaces() {
        val file = projectFile("src/main/res/layout/activity_file_manage.xml").readText()
        val import = projectFile("src/main/res/layout/activity_import_book.xml").readText()
        assertTrue(file.contains("@drawable/novel_helper_preference_card"))
        assertTrue(file.contains("android:layout_height=\"40dp\""))
        assertTrue(import.contains("@drawable/novel_helper_preference_card"))
        assertTrue(import.contains("app:cornerRadius=\"12dp\""))
    }

    @Test
    fun bookshelfConfigUsesSingleRoundedSettingsSurface() {
        val layout = projectFile("src/main/res/layout/dialog_bookshelf_config.xml").readText()
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.contains("android:elevation=\"2dp\""))
        assertTrue(layout.contains("@+id/ll_read_progress"))
        assertTrue(layout.contains("@+id/layout_columns_bottom"))
    }

    @Test
    fun sourceEditorUsesRoundedOptionAndTabSurfaces() {
        val layout = projectFile("src/main/res/layout/activity_book_source_edit.xml").readText()
        assertTrue(layout.contains("app:cardCornerRadius=\"18dp\""))
        assertTrue(layout.contains("app:cardElevation=\"1dp\""))
        assertTrue(layout.split("@drawable/novel_helper_search_field").size - 1 >= 2)
        assertTrue(layout.contains("@+id/recycler_view"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
