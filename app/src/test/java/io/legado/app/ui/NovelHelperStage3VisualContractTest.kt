package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage3VisualContractTest {

    @Test
    fun appUiIsPinnedToSimplifiedChinese() {
        val wrapper = projectFile("src/main/java/io/legado/app/base/AppContextWrapper.kt").readText()
        assertTrue(wrapper.contains("Locale.SIMPLIFIED_CHINESE"))
        assertFalse(wrapper.contains("getPrefString(PreferKey.language)"))

        val otherPrefs = projectFile("src/main/res/xml/pref_config_other.xml").readText()
        assertFalse(otherPrefs.contains("android:key=\"language\""))

        val zh = projectFile("src/main/res/values-zh/strings.xml").readText()
        assertTrue(zh.contains("<string name=\"menu_backup\">主页</string>"))
    }

    @Test
    fun bookInfoUsesNovelHelperDetailSurfaces() {
        val portrait = projectFile("src/main/res/layout/activity_book_info.xml").readText()
        val landscape = projectFile("src/main/res/layout-land/activity_book_info.xml").readText()
        val activity = projectFile("src/main/java/io/legado/app/ui/book/info/BookInfoActivity.kt").readText()

        assertFalse(portrait.contains("io.legado.app.ui.widget.image.ArcView"))
        assertTrue(portrait.contains("@drawable/novel_helper_book_sheet"))
        assertTrue(portrait.contains("@drawable/novel_helper_section_outline"))
        assertTrue(landscape.contains("@drawable/novel_helper_section_outline"))
        assertTrue(activity.contains("GradientDrawable"))
        assertFalse(activity.contains("Loading....."))
        assertFalse(activity.contains("Unexpected webFileData"))
    }

    @Test
    fun tocAndDynamicStatesUseNovelHelperVisuals() {
        val tocLayout = projectFile("src/main/res/layout/fragment_chapter_list.xml").readText()
        val tocFragment = projectFile("src/main/java/io/legado/app/ui/book/toc/ChapterListFragment.kt").readText()
        val error = projectFile("src/main/res/layout/view_error.xml").readText()
        val loading = projectFile("src/main/res/layout/view_loading.xml").readText()
        val dynamic = projectFile("src/main/java/io/legado/app/ui/widget/dynamiclayout/DynamicFrameLayout.kt").readText()

        assertTrue(tocLayout.contains("@drawable/novel_helper_toc_up"))
        assertTrue(tocLayout.contains("@drawable/novel_helper_toc_down"))
        assertTrue(tocFragment.contains("cornerRadius = 18.dpToPx().toFloat()"))
        assertTrue(error.contains("@drawable/novel_helper_state_card"))
        assertTrue(loading.contains("@drawable/novel_helper_state_card"))
        assertTrue(dynamic.contains("R.drawable.novel_helper_state_mark"))
    }

    private fun projectFile(pathInApp: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        val appDir = generateSequence(File(userDir)) { it.parentFile }
            .map { File(it, "app") }
            .first(File::isDirectory)
        return File(appDir, pathInApp)
    }
}
