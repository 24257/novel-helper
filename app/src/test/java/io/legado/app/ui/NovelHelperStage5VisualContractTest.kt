package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage5VisualContractTest {

    @Test
    fun searchResultUsesUnifiedNovelHelperCard() {
        val layout = projectFile("src/main/res/layout/item_search.xml").readText()
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(layout.contains("android:elevation=\"1dp\""))
        assertTrue(layout.contains("android:padding=\"6dp\""))
    }

    @Test
    fun profileAndAboutUseLayeredBrandCards() {
        val profile = projectFile("src/main/res/layout/fragment_my_config.xml").readText()
        val about = projectFile("src/main/res/layout/activity_about.xml").readText()
        assertTrue(profile.contains("android:background=\"@color/background\""))
        assertTrue(profile.contains("android:elevation=\"2dp\""))
        assertTrue(about.contains("@drawable/novel_helper_preference_card"))
        assertTrue(about.contains("@drawable/novel_helper_brand_mark"))
    }

    @Test
    fun readerMenuUsesRoundedDynamicPanelWithoutChangingEInkFallback() {
        val layout = projectFile("src/main/res/layout/view_read_menu.xml").readText()
        val source = projectFile("src/main/java/io/legado/app/ui/book/read/ReadMenu.kt").readText()
        assertTrue(layout.contains("android:id=\"@+id/ll_bottom_bg\""))
        assertTrue(layout.contains("android:elevation=\"8dp\""))
        assertTrue(layout.contains("app:radius=\"12dp\""))
        assertTrue(layout.contains("tools:text=\"章节地址\""))
        assertTrue(source.contains("val panelRadius = 24F.dpToPx()"))
        assertTrue(source.contains("cornerRadii = floatArrayOf("))
        assertTrue(source.contains("llBottomBg.setBackgroundResource(R.drawable.bg_eink_border_top)"))
        assertTrue(source.contains("brightnessBackground.cornerRadius = 18F.dpToPx()"))
    }

    @Test
    fun editorPreviewCopyNoLongerUsesObviousEnglishPlaceholders() {
        val task = projectFile("src/main/res/layout/activity_auto_task_debug.xml").readText()
        val record = projectFile("src/main/res/layout/activity_read_record.xml").readText()
        val recordItem = projectFile("src/main/res/layout/item_read_record.xml").readText()
        assertTrue(task.contains("tools:text=\"正在运行任务…\""))
        assertTrue(record.contains("tools:text=\"小说名称\""))
        assertTrue(record.contains("tools:text=\"阅读时长\""))
        assertTrue(recordItem.contains("tools:text=\"最后阅读时间\""))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
