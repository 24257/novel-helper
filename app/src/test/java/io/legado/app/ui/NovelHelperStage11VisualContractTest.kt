package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage11VisualContractTest {

    @Test
    fun bookChangeSourceUsesDarkGoldCardsWithoutLegacyDividers() {
        val layout = projectFile("src/main/res/layout/dialog_book_change_source.xml").readText()
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/changesource/ChangeBookSourceDialog.kt"
        ).readText()

        assertTrue(layout.contains("android:background=\"@color/background\""))
        assertTrue(layout.contains("android:id=\"@+id/recycler_view\""))
        assertTrue(layout.contains("android:clipToPadding=\"false\""))
        assertTrue(layout.contains("android:paddingHorizontal=\"8dp\""))
        assertTrue(layout.contains("android:id=\"@+id/ll_bottom_bar\""))
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.split("app:tint=\"@color/xuanjuan_gold_soft\"").size - 1 >= 2)
        assertFalse(dialog.contains("addItemDecoration(VerticalDivider(requireContext()))"))
        assertTrue(dialog.contains("scrollToDurSource()"))
        assertTrue(dialog.contains("viewModel.startOrStopSearch()"))
        assertTrue(dialog.contains("viewModel.changeSource(book)"))
    }

    @Test
    fun chapterChangeSourceUsesLayeredCardPanelsAndKeepsBatchControls() {
        val layout = projectFile("src/main/res/layout/dialog_chapter_change_source.xml").readText()
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/changesource/ChangeChapterSourceDialog.kt"
        ).readText()

        assertTrue(layout.contains("android:id=\"@+id/ll_bottom_bar\""))
        assertTrue(layout.contains("android:id=\"@+id/cl_toc\""))
        assertTrue(layout.contains("android:id=\"@+id/fl_hide_toc\""))
        assertTrue(layout.contains("android:id=\"@+id/ll_batch_actions\""))
        assertTrue(layout.split("@drawable/novel_helper_preference_card").size - 1 >= 3)
        assertTrue(layout.contains("@drawable/novel_helper_search_field"))
        assertTrue(layout.contains("android:id=\"@+id/recycler_view_toc\""))
        assertTrue(layout.contains("android:clipToPadding=\"false\""))
        assertFalse(dialog.contains("addItemDecoration(VerticalDivider(requireContext()))"))
        assertTrue(dialog.contains("viewModel.loadToc(searchBook.toBook())"))
        assertTrue(dialog.contains("cacheSelectedChapters()"))
        assertTrue(dialog.contains("viewModel.startAutomation("))
    }

    @Test
    fun sourceResultRowsUseXuanjuanCardLanguageAndChinesePreviews() {
        val layout = projectFile("src/main/res/layout/item_change_source.xml").readText()

        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.contains("android:clipToOutline=\"true\""))
        assertTrue(layout.contains("android:elevation=\"1dp\""))
        assertTrue(layout.contains("android:foreground=\"?android:attr/selectableItemBackground\""))
        listOf(
            "@+id/view_selected_background",
            "@+id/iv_good",
            "@+id/iv_bad",
            "@+id/tv_origin",
            "@+id/tv_author",
            "@+id/tv_last",
            "@+id/tv_current_chapter_word_count",
            "@+id/tv_respond_time",
            "@+id/iv_checked",
        ).forEach { id -> assertTrue(layout.contains(id)) }
        assertTrue(layout.contains("app:tint=\"@color/xuanjuan_gold_soft\""))
        listOf("示例书源", "作者", "最新章节", "当前章字数", "响应耗时")
            .forEach { preview -> assertTrue(layout.contains("tools:text=\"$preview\"")) }
        assertFalse(layout.contains("tools:text=\"bookSourceName\""))
        assertFalse(layout.contains("tools:text=\"latest chapter name\""))
    }

    @Test
    fun currentSourceSelectionContractRemainsUntouched() {
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/changesource/ChangeBookSourceAdapter.kt"
        ).readText()
        assertTrue(adapter.contains("viewSelectedBackground"))
        assertTrue(adapter.contains("ColorUtils.withAlpha(context.accentColor, 0.1f)"))
        assertTrue(adapter.contains("ivChecked.visible()"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
