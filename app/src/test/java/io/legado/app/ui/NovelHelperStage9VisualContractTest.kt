package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage9VisualContractTest {

    @Test
    fun sharedRecyclerDialogUsesDarkSurfaceStateCardAndRoundedFooter() {
        val layout = projectFile("src/main/res/layout/dialog_recycler_view.xml").readText()
        assertTrue(layout.contains("android:background=\"@color/background\""))
        assertTrue(layout.contains("@+id/ll_empty"))
        assertTrue(layout.contains("android:layout_width=\"280dp\""))
        assertTrue(layout.contains("@drawable/novel_helper_state_card"))
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.contains("android:elevation=\"2dp\""))
        assertTrue(layout.contains("@color/xuanjuan_gold_soft"))
    }

    @Test
    fun importRowsUseCardSurfaceAndGoldStatusWithoutBreakingCommentContract() {
        val layout = projectFile("src/main/res/layout/item_source_import.xml").readText()
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.contains("android:elevation=\"1dp\""))
        assertTrue(layout.contains("@+id/tv_source_state"))
        assertTrue(layout.contains("@drawable/novel_helper_section_outline"))
        assertTrue(layout.contains("@color/xuanjuan_gold_soft"))
        assertTrue(layout.contains("@+id/show_comment"))
        assertTrue(layout.contains("android:maxLines=\"3\""))
    }

    @Test
    fun bookSourcePageAndRowsUseLayeredCardsAndKeepSourceControls() {
        val activity = projectFile("src/main/res/layout/activity_book_source.xml").readText()
        val item = projectFile("src/main/res/layout/item_book_source.xml").readText()
        assertTrue(activity.contains("android:background=\"@color/background\""))
        assertTrue(activity.contains("android:clipToPadding=\"false\""))
        assertTrue(item.contains("@drawable/novel_helper_preference_card"))
        assertTrue(item.contains("android:elevation=\"1dp\""))
        listOf(
            "@+id/cb_book_source",
            "@+id/tv_js_badge",
            "@+id/tv_source_url",
            "@+id/selection_accent",
            "@+id/swt_enabled",
            "@+id/iv_edit",
            "@+id/iv_menu_more",
            "@+id/iv_debug_text",
            "@+id/iv_progressBar",
        ).forEach { id -> assertTrue(item.contains(id)) }
        assertTrue(item.contains("tools:text=\"示例域名\""))
        assertTrue(item.contains("@drawable/novel_helper_section_outline"))
        assertTrue(item.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
