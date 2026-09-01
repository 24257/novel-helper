package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage10VisualContractTest {

    @Test
    fun groupManagementRowsUseXuanjuanCardsAndKeepControls() {
        val bookGroup = projectFile("src/main/res/layout/item_book_group_manage.xml").readText()
        val group = projectFile("src/main/res/layout/item_group_manage.xml").readText()

        listOf(bookGroup, group).forEach { layout ->
            assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
            assertTrue(layout.contains("android:elevation=\"1dp\""))
            assertTrue(layout.contains("@+id/tv_group"))
            assertTrue(layout.contains("@+id/tv_edit"))
            assertTrue(layout.contains("@color/xuanjuan_gold_soft"))
        }
        assertTrue(bookGroup.contains("@+id/sw_show"))
        assertTrue(bookGroup.contains("tools:text=\"默认分组\""))
        assertTrue(group.contains("@+id/tv_del"))
    }

    @Test
    fun groupDialogsKeepBusinessActionsButDropLegacyDividersAndFlatRows() {
        val paths = listOf(
            "src/main/java/io/legado/app/ui/book/group/GroupManageDialog.kt",
            "src/main/java/io/legado/app/ui/book/source/manage/GroupManageDialog.kt",
            "src/main/java/io/legado/app/ui/rss/source/manage/GroupManageDialog.kt",
            "src/main/java/io/legado/app/ui/replace/GroupManageDialog.kt",
        )
        paths.forEach { path ->
            val source = projectFile(path).readText()
            assertTrue(source.contains("R.drawable.novel_helper_preference_card"))
            assertFalse(source.contains("addItemDecoration(VerticalDivider"))
            assertTrue(source.contains("tvGroup.text"))
        }
        val bookGroup = projectFile(paths.first()).readText()
        assertTrue(bookGroup.contains("itemTouchCallback.isCanDrag = true"))
        assertTrue(bookGroup.contains("viewModel.upGroup"))
        val sourceGroup = projectFile(paths[1]).readText()
        assertTrue(sourceGroup.contains("viewModel.addGroup"))
        assertTrue(sourceGroup.contains("viewModel.delGroup"))
    }

    @Test
    fun themeLogAndIconRowsUseDarkGoldCardLanguage() {
        val theme = projectFile("src/main/res/layout/item_theme_config.xml").readText()
        val log = projectFile("src/main/res/layout/item_app_log.xml").readText()
        val icon = projectFile("src/main/res/layout/item_icon_preference.xml").readText()

        listOf(theme, log, icon).forEach { layout ->
            assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
            assertTrue(layout.contains("android:elevation=\"1dp\""))
        }
        assertTrue(theme.contains("tools:text=\"玄卷暗金\""))
        assertTrue(theme.contains("@+id/iv_share"))
        assertTrue(theme.contains("@+id/iv_delete"))
        assertTrue(log.contains("@color/xuanjuan_gold_soft"))
        assertTrue(log.contains("tools:text=\"日志内容将在这里显示\""))
        assertTrue(icon.contains("android:contentDescription=\"@string/change_icon\""))
        assertFalse(icon.contains("android:contentDescription=\"ICON\""))
        assertTrue(icon.contains("tools:text=\"玄卷图标\""))
    }

    @Test
    fun rssEmptyStateUsesXuanjuanStateCardAndChinesePreview() {
        val layout = projectFile("src/main/res/layout/fragment_rss.xml").readText()
        assertTrue(layout.contains("android:background=\"@color/background\""))
        assertTrue(layout.contains("@drawable/novel_helper_state_card"))
        assertTrue(layout.contains("android:layout_width=\"280dp\""))
        assertTrue(layout.contains("android:padding=\"20dp\""))
        assertTrue(layout.contains("tools:text=\"暂无 RSS 内容\""))
        assertFalse(layout.contains("tools:text=\"TextView\""))
    }

    @Test
    fun sharedEmptyStateCardOnlyDrawsWhenMessageIsVisible() {
        val layout = projectFile("src/main/res/layout/dialog_recycler_view.xml").readText()
        val emptyContainer = layout.substringAfter("android:id=\"@+id/ll_empty\"")
            .substringBefore(">")
        val message = layout.substringAfter("android:id=\"@+id/tv_msg\"")
            .substringBefore("/>")

        assertFalse(emptyContainer.contains("@drawable/novel_helper_state_card"))
        assertTrue(message.contains("@drawable/novel_helper_state_card"))
        assertTrue(message.contains("android:visibility=\"gone\""))
        assertTrue(message.contains("android:padding=\"20dp\""))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
