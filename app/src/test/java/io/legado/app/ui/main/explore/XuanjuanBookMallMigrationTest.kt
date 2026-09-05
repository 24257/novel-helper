package io.legado.app.ui.main.explore

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanBookMallMigrationTest {

    @Test
    fun bookMallRestoresOriginalExploreCapabilitiesBehindXuanjuanUi() {
        val fragment = projectFile("src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt").readText()
        val adapter = projectFile("src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt").readText()
        val layout = projectFile("src/main/res/layout/fragment_explore.xml").readText()
        val menu = projectFile("src/main/res/menu/main_explore.xml").readText()

        assertTrue(fragment.contains("ExploreAdapter(requireContext(), this)"))
        assertTrue(fragment.contains("flowExplore(searchKey)"))
        assertTrue(fragment.contains("initAggregatePortal()"))
        assertTrue(layout.contains("@+id/aggregate_portal"))
        assertTrue(layout.contains("tools:listitem=\"@layout/item_find_book\""))
        assertTrue(menu.substringAfter("@+id/menu_group").substringBefore("</item>")
            .contains("android:visible=\"true\""))

        listOf("Type.url", "Type.button", "Type.text", "Type.toggle", "Type.select").forEach {
            assertTrue(adapter.contains(it))
        }
        listOf("login", "search", "refresh", "edit", "top", "delete").forEach {
            assertTrue(adapter.contains("\"$it\""))
        }
        assertTrue(adapter.contains("xuanjuan_explore_chip"))
    }

    @Test
    fun builtinSourceMigrationVersionIsBumpedForExploreBackfill() {
        val localConfig = projectFile("src/main/java/io/legado/app/help/config/LocalConfig.kt").readText()
        assertTrue(localConfig.contains("!isLastVersion(20, \"bookSourceVersion\")"))
        val defaultData = projectFile("src/main/java/io/legado/app/help/DefaultData.kt").readText()
        assertTrue(defaultData.contains("bqqugeExploreUrl"))
        assertTrue(defaultData.contains("cuocengExploreUrl"))
        assertTrue(defaultData.contains("dingdian100ExploreUrl"))
        assertTrue(defaultData.contains("exploreUrl = bqqugeExploreUrl"))
        assertTrue(defaultData.contains("exploreUrl = cuocengExploreUrl"))
        assertTrue(defaultData.contains("exploreUrl = dingdian100ExploreUrl"))
        val hetushu = projectFile("src/main/assets/defaultData/bookSources/hetushu.js").readText()
        assertTrue(hetushu.contains("https://m.hetushu.com/top/index.php"))
        assertTrue(hetushu.contains("https://www.hetushu.com/book/\" + match[1] + \"/index.html"))
        assertTrue(hetushu.contains("mobileMatch"))
        assertTrue(hetushu.contains("Mobile Safari/537.36"))
    }

    @Test
    fun selectedDiscoverySourcesRequireRealCovers() {
        val sourceFiles = listOf("bqquge.js", "cuoceng.js", "dingdian100.js")
        sourceFiles.forEach { fileName ->
            val source = projectFile("src/main/assets/defaultData/bookSources/$fileName").readText()
            assertTrue("$fileName must implement explore", source.contains("function explore"))
            assertTrue("$fileName must emit coverUrl", source.contains("coverUrl"))
            assertTrue("$fileName must reject coverless explore rows",
                source.contains("!coverUrl") ||
                    source.contains("if (!coverUrl) continue") ||
                    source.contains("if(!coverUrl)continue") ||
                    source.contains("coverUrl)) continue") ||
                    source.contains("coverUrl))continue"))
        }
        assertTrue(projectFile("src/main/assets/defaultData/bookSources/cuoceng.js").readText().contains("java.ajaxAll"))
        val sto66 = projectFile("src/main/assets/defaultData/bookSources/sto66.js").readText()
        assertTrue(sto66.contains("complete.length < 12"))
        assertTrue(sto66.contains("complete.push(book)"))

        listOf("shudugu.js", "hetushu.js").forEach { fileName ->
            val source = projectFile("src/main/assets/defaultData/bookSources/$fileName").readText()
            assertTrue(
                "$fileName must filter coverless discovery rows",
                source.contains("trimText(book.coverUrl).length > 0"),
            )
        }
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
