package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage12VisualContractTest {

    @Test
    fun bookmarkAndHighlightListsKeepCardsWithoutLegacyDividers() {
        val layout = projectFile("src/main/res/layout/fragment_bookmark.xml").readText()
        val bookmark = projectFile("src/main/java/io/legado/app/ui/book/toc/BookmarkFragment.kt").readText()
        val highlight = projectFile("src/main/java/io/legado/app/ui/book/toc/HighlightFragment.kt").readText()

        assertTrue(layout.contains("android:background=\"@color/background\""))
        assertTrue(layout.contains("android:paddingTop=\"4dp\""))
        assertTrue(layout.contains("android:paddingBottom=\"4dp\""))
        listOf(bookmark, highlight).forEach { source ->
            assertFalse(source.contains("addItemDecoration(VerticalDivider(requireContext()))"))
            assertTrue(source.contains("binding.recyclerView.adapter = adapter"))
            assertTrue(source.contains("applyNavigationBarPadding()"))
        }
        listOf("item_bookmark.xml", "item_highlight.xml").forEach { file ->
            val item = projectFile("src/main/res/layout/$file").readText()
            assertTrue(item.contains("androidx.cardview.widget.CardView"))
            assertTrue(item.contains("app:cardBackgroundColor=\"@color/background_card\""))
        }
    }

    @Test
    fun groupPickerUsesCardsButKeepsSelectionAndDragLogic() {
        val dialogLayout = projectFile("src/main/res/layout/dialog_book_group_picker.xml").readText()
        val itemLayout = projectFile("src/main/res/layout/item_group_select.xml").readText()
        val source = projectFile("src/main/java/io/legado/app/ui/book/group/GroupSelectDialog.kt").readText()

        assertTrue(dialogLayout.contains("android:background=\"@color/background\""))
        assertTrue(dialogLayout.contains("android:paddingHorizontal=\"8dp\""))
        assertTrue(dialogLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(itemLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(itemLayout.contains("android:elevation=\"1dp\""))
        assertTrue(itemLayout.contains("@color/xuanjuan_gold_soft"))
        assertTrue(itemLayout.contains("tools:text=\"默认分组\""))
        assertFalse(source.contains("VerticalDivider"))
        assertFalse(source.contains("root.setBackgroundColor(context.backgroundColor)"))
        assertTrue(source.contains("itemTouchCallback.isCanDrag = true"))
        assertTrue(source.contains("cbGroup.isChecked = (groupId and item.groupId) > 0"))
        assertTrue(source.contains("viewModel.upGroup"))
    }

    @Test
    fun filePickerUsesCardRowsAndLocalizedRootBreadcrumb() {
        val dialogLayout = projectFile("src/main/res/layout/dialog_file_chooser.xml").readText()
        val itemLayout = projectFile("src/main/res/layout/item_file_picker.xml").readText()
        val source = projectFile("src/main/java/io/legado/app/ui/file/FilePickerDialog.kt").readText()

        assertTrue(dialogLayout.contains("android:layout_height=\"40dp\""))
        assertTrue(dialogLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(dialogLayout.contains("android:id=\"@+id/rv_file\""))
        assertTrue(dialogLayout.contains("android:padding=\"8dp\""))
        assertTrue(dialogLayout.contains("@drawable/novel_helper_state_card"))
        assertTrue(dialogLayout.contains("android:layout_width=\"280dp\""))
        assertTrue(dialogLayout.contains("tools:text=\"当前目录暂无可选文件\""))
        assertTrue(itemLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(itemLayout.contains("android:elevation=\"1dp\""))
        assertTrue(itemLayout.contains("tools:text=\"示例文件.txt\""))
        assertFalse(source.contains("VerticalDivider"))
        assertTrue(source.contains("textView.text = \"根目录\""))
        assertTrue(source.contains("R.drawable.novel_helper_search_field"))
        assertTrue(source.contains("R.drawable.novel_helper_preference_card"))
        assertTrue(source.contains("selectFile = item"))
        assertTrue(source.contains("viewModel.allowExtensions"))
    }

    @Test
    fun textSelectionMenuConfigUsesCardsAndKeepsReorderPersistence() {
        val itemLayout = projectFile("src/main/res/layout/item_text_select_menu_config.xml").readText()
        val source = projectFile(
            "src/main/java/io/legado/app/ui/book/read/config/TextSelectMenuConfigDialog.kt"
        ).readText()

        assertTrue(itemLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(itemLayout.contains("android:elevation=\"1dp\""))
        assertTrue(itemLayout.contains("tools:text=\"浮动栏\""))
        assertTrue(itemLayout.contains("tools:text=\"复制\""))
        assertTrue(itemLayout.contains("@color/xuanjuan_gold_soft"))
        assertFalse(source.contains("VerticalDivider"))
        assertTrue(source.contains("itemTouchHelper.startDrag(holder)"))
        assertTrue(source.contains("moveAcrossDivider(position)"))
        assertTrue(source.contains("saveTextSelectMenuConfig("))
        assertTrue(source.contains("commitRows(bar, more)"))
    }

    @Test
    fun chapterDirectoryUsesDarkCardsWithoutChangingTocStateLogic() {
        val item = projectFile("src/main/res/layout/item_chapter_list.xml").readText()
        val fragmentLayout = projectFile("src/main/res/layout/fragment_chapter_list.xml").readText()
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/book/toc/ChapterListFragment.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/toc/ChapterListAdapter.kt"
        ).readText()
        val changeAdapter = projectFile(
            "src/main/java/io/legado/app/ui/book/changesource/ChangeChapterTocAdapter.kt"
        ).readText()

        assertTrue(item.contains("@drawable/novel_helper_preference_card"))
        assertTrue(item.contains("android:clipToOutline=\"true\""))
        assertTrue(item.contains("android:elevation=\"1dp\""))
        assertTrue(item.contains("android:foreground=\"?android:attr/selectableItemBackground\""))
        assertTrue(item.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
        assertTrue(fragmentLayout.contains("android:background=\"@color/background\""))
        assertTrue(fragmentLayout.contains("android:paddingTop=\"4dp\""))
        assertTrue(fragmentLayout.contains("android:paddingBottom=\"4dp\""))
        assertFalse(fragment.contains("VerticalDivider"))

        listOf(adapter, changeAdapter).forEach { source ->
            assertTrue(source.contains("R.drawable.novel_helper_preference_card"))
            assertTrue(source.contains("R.drawable.novel_helper_search_field"))
            assertFalse(source.contains("ThemeUtils.resolveDrawable"))
        }
        assertTrue(adapter.contains("item.containsCurrentChapter"))
        assertTrue(adapter.contains("callback.onVolumeToggled"))
        assertTrue(adapter.contains("audioCacheKeys.contains"))
        assertTrue(changeAdapter.contains("cbSelected.isChecked = isSelected"))
        assertTrue(changeAdapter.contains("callback.selectionChanged()"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
