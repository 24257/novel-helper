package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage2VisualContractTest {
    @Test fun launcherAndPrimarySurfacesUseNovelHelperBranding() {
        val manifest = projectFile("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:icon=\"@mipmap/novel_helper_launcher\""))
        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/novel_helper_launcher\""))
        assertFalse(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        val myLayout = projectFile("src/main/res/layout/fragment_my_config.xml").readText()
        assertTrue(myLayout.contains("@drawable/novel_helper_profile_header"))
        assertTrue(myLayout.contains("@drawable/novel_helper_brand_mark"))
        assertTrue(myLayout.contains("@+id/tv_version"))
        val preferences = projectFile("src/main/res/xml/pref_main.xml").readText()
        assertTrue(preferences.contains("@layout/novel_helper_preference"))
        assertTrue(preferences.contains("@layout/novel_helper_preference_category"))
        assertTrue(preferences.contains("io.legado.app.ui.main.my.NovelHelperPreference"))
        assertTrue(preferences.contains("io.legado.app.ui.main.my.NovelHelperSwitchPreference"))
        assertTrue(preferences.contains("io.legado.app.ui.main.my.NovelHelperNameListPreference"))
        assertTrue(preferences.contains("io.legado.app.ui.main.my.NovelHelperPreferenceCategory"))
        assertFalse(preferences.contains("@drawable/ic_cfg_"))

        val row = projectFile("src/main/res/layout/novel_helper_preference.xml").readText()
        assertTrue(row.contains("@+id/preference_title"))
        assertTrue(row.contains("@+id/preference_desc"))
        assertTrue(row.contains("@+id/preference_icon"))
        assertTrue(row.contains("@+id/preference_widget"))
    }
    @Test fun searchAndBookshelfHeadersUseNovelHelperVisuals() {
        val search = projectFile("src/main/res/layout/view_search.xml").readText()
        assertTrue(search.contains("@drawable/novel_helper_search_field"))
        assertTrue(search.contains("@drawable/novel_helper_search_go"))
        assertTrue(search.contains("@drawable/novel_helper_search_close"))
        assertTrue(search.contains("android:layout_height=\"44dp\""))
        listOf("fragment_bookshelf1.xml", "fragment_bookshelf2.xml").forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue(layout.contains("app:title=\"@string/app_name\""))
            assertTrue(layout.contains("app:subtitle=\"@string/xuanjuan_library\""))
            assertTrue(layout.contains("@layout/view_xuanjuan_shelf_ornament"))
        }
    }
    private fun projectFile(pathInApp: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        val appDir = generateSequence(File(userDir)) { it.parentFile }.map { File(it, "app") }.first(File::isDirectory)
        return File(appDir, pathInApp)
    }
}
