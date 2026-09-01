package io.legado.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage8VisualContractTest {

    @Test
    fun xuanjuanDefaultPaletteIsDarkGoldInsteadOfLegacyTealCoral() {
        val brand = projectFile("src/main/res/values/novel_helper_colors.xml").readText()
        val colors = projectFile("src/main/res/values/colors.xml").readText()
        val defaults = projectFile("src/main/assets/defaultData/themeConfig.json").readText()

        assertTrue(brand.contains("<color name=\"xuanjuan_gold\">#C79A4A</color>"))
        assertTrue(brand.contains("<color name=\"xuanjuan_surface\">#100D0B</color>"))
        assertFalse(brand.contains("#2E6F73"))
        assertFalse(brand.contains("#D97757"))
        assertTrue(colors.contains("<color name=\"background\">#0B0908</color>"))
        assertTrue(colors.contains("<color name=\"primaryText\">@color/xuanjuan_text_primary</color>"))
        assertTrue(defaults.contains("\"themeName\": \"玄卷暗金\""))
        assertTrue(defaults.contains("\"accentColor\": \"#C79A4A\""))
    }

    @Test
    fun cardsAndNavigationUseDarkSurfacesWithGoldDetails() {
        val card = projectFile("src/main/res/drawable/novel_helper_preference_card.xml").readText()
        val dock = projectFile("src/main/res/drawable/novel_helper_bottom_bar.xml").readText()
        val nav = projectFile("src/main/java/io/legado/app/lib/theme/view/ThemeBottomNavigationVIew.kt").readText()

        assertTrue(card.contains("@color/xuanjuan_surface_raised"))
        assertTrue(card.contains("@color/novel_helper_card_stroke"))
        assertTrue(dock.contains("@color/xuanjuan_surface_high"))
        assertTrue(nav.contains("R.color.xuanjuan_surface_raised"))
        assertTrue(nav.contains("R.color.xuanjuan_gold_soft"))
        assertTrue(nav.contains("AppConfig.isEInkMode || transparentNavBar"))
    }

    @Test
    fun runtimeThemeDefaultsMigrateFromLegacyMaterialColorsToXuanjuan() {
        val theme = projectFile("src/main/java/io/legado/app/help/config/ThemeConfig.kt").readText()

        assertTrue(theme.contains("migrateLegacyDefaultTheme(context)"))
        assertTrue(theme.contains("R.color.md_brown_500), xuanjuanDayPrimary"))
        assertTrue(theme.contains("R.color.md_red_600), xuanjuanDayAccent"))
        assertTrue(theme.contains("R.color.md_blue_grey_600), xuanjuanNightPrimary"))
        assertTrue(theme.contains("getPrefInt(PreferKey.cPrimary, xuanjuanDayPrimary)"))
        assertTrue(theme.contains("getPrefInt(PreferKey.cAccent, xuanjuanDayAccent)"))
        assertTrue(theme.contains("getPrefInt(PreferKey.cNPrimary, xuanjuanNightPrimary)"))
        assertFalse(theme.contains("if (!ColorUtils.isColorLight(background))"))
    }

    @Test
    fun highFrequencyShellPagesExplicitlyUseDarkBackgroundsAndBrandSurfaces() {
        listOf(
            "activity_main.xml",
            "fragment_bookshelf2.xml",
            "fragment_my_config.xml",
            "activity_about.xml",
            "activity_book_search.xml",
            "fragment_explore.xml",
            "activity_explore_show.xml",
            "activity_book_info.xml",
        ).forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue("$name should use the shell background", layout.contains("android:background=\"@color/background\""))
        }
        val bookInfo = projectFile("src/main/res/layout/activity_book_info.xml").readText()
        assertTrue(bookInfo.contains("android:id=\"@+id/fl_action\""))
        assertTrue(bookInfo.contains("android:background=\"@drawable/novel_helper_preference_card\""))

        val loadMore = projectFile("src/main/res/layout/view_load_more.xml").readText()
        assertTrue(loadMore.contains("android:textColor=\"@color/secondaryText\""))
    }

    @Test
    fun bookshelfUsesXuanjuanOrnamentSpinesAndGoldProgress() {
        listOf("fragment_bookshelf1.xml", "fragment_bookshelf2.xml").forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue(layout.contains("@layout/view_xuanjuan_shelf_ornament"))
            assertTrue(layout.contains("app:subtitle=\"@string/xuanjuan_library\""))
        }

        val ornament = projectFile("src/main/res/layout/view_xuanjuan_shelf_ornament.xml").readText()
        assertTrue(ornament.contains("@+id/tv_xuanjuan_library_mark"))
        assertTrue(ornament.contains("@string/xuanjuan_library_mark"))
        assertTrue(ornament.contains("@color/xuanjuan_gold_soft"))

        val header = projectFile("src/main/res/layout/view_bookshelf_header.xml").readText()
        assertTrue(header.contains("@+id/tv_shelf_kicker"))
        assertTrue(header.contains("@string/xuanjuan_shelf_overview"))
        assertTrue(header.contains("@color/xuanjuan_gold_soft"))

        listOf(
            "item_bookshelf_grid.xml",
            "item_bookshelf_grid2.xml",
            "item_bookshelf_list.xml",
            "item_bookshelf_list2.xml",
        ).forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue(layout.contains("@+id/xuanjuan_spine"))
            assertTrue(layout.contains("@drawable/xuanjuan_shelf_spine"))
            assertTrue(layout.contains("app:indicatorColor=\"@color/xuanjuan_gold_soft\""))
        }
    }

    @Test
    fun profileSearchExploreAndBookDetailCarryXuanjuanIdentity() {
        val profile = projectFile("src/main/res/layout/fragment_my_config.xml").readText()
        val about = projectFile("src/main/res/layout/activity_about.xml").readText()
        val profileHeader = projectFile("src/main/res/drawable/novel_helper_profile_header.xml").readText()
        val searchField = projectFile("src/main/res/drawable/novel_helper_search_field.xml").readText()
        val searchItem = projectFile("src/main/res/layout/item_search.xml").readText()
        val exploreHeader = projectFile("src/main/res/drawable/bg_find_book_group.xml").readText()
        val findItem = projectFile("src/main/res/layout/item_find_book.xml").readText()

        assertTrue(profile.contains("@+id/tv_brand_tagline"))
        assertTrue(profile.contains("@string/welcome_tagline"))
        assertTrue(profileHeader.contains("@color/xuanjuan_gold_outline"))
        assertTrue(about.contains("@+id/tv_brand_tagline"))
        assertTrue(about.contains("@drawable/xuanjuan_ornament_line"))
        assertTrue(searchField.contains("@color/xuanjuan_gold_outline"))
        assertTrue(searchItem.contains("@+id/xuanjuan_result_spine"))
        assertTrue(searchItem.contains("@drawable/xuanjuan_shelf_spine"))
        assertTrue(exploreHeader.contains("@color/xuanjuan_surface_high"))
        assertTrue(exploreHeader.contains("@color/xuanjuan_gold_dim"))
        assertTrue(findItem.contains("tools:text=\"示例书源\""))
        assertTrue(findItem.contains("app:tint=\"@color/xuanjuan_gold_soft\""))

        listOf(
            "src/main/res/layout/activity_book_info.xml",
            "src/main/res/layout-land/activity_book_info.xml",
        ).forEach { path ->
            val detail = projectFile(path).readText()
            assertTrue(detail.contains("@+id/tv_xuanjuan_book_detail_mark"))
            assertTrue(detail.contains("@string/xuanjuan_book_detail_mark"))
            assertTrue(detail.split("app:tint=\"@color/xuanjuan_gold_soft\"").size - 1 >= 5)
        }
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
