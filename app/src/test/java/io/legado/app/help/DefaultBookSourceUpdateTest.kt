package io.legado.app.help

import io.legado.app.data.entities.BookSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultBookSourceUpdateTest {

    @Test
    fun packagedBuiltinSourceManifestHasExpectedCountAndUrls() {
        val expected = setOf(
            "https://www.biquge432.com",
            "https://www.dingdian100.com",
            "https://www.dingdian678.com",
            "https://www.xbiquge2345.com",
            "https://www.lengsk.com",
            "https://www.00shu.la/",
            "https://wujixsw.info/",
            "http://www.gdbzkz.com/",
            "http://www.xinbqg.org/",
            "https://www.yeban360.com",
            "https://zhongtianwen.cn",
            "http://www.feisuwx.org",
            "http://www.3yt.la",
            "http://www.aixiawx.com/",
            "http://www.yetianlian.info/",
            "https://www.biqusa.com/",
            "https://www.yingsx.com/",
            "https://www.14oz.net/",
            "https://www.jrleaguepasadena.org/",
            "https://m.huaboedu.com/",
            "http://www.xs5300.org/",
            "http://www.92xs.info",
            "http://wap.qiqixs.info",
            "http://www.23uswx.la",
            "https://www.shenhuazhihou.com",
            "https://www.zhizhuxs.com",
            "https://m.kudushu.org",
            "http://wap.wangshuge.la",
            "http://www.qudushu.com",
        )
        assertEquals(37, NOVEL_HELPER_BUILTIN_SOURCE_URLS.size)
        assertEquals(
            NOVEL_HELPER_BUILTIN_SOURCE_URLS.size,
            NOVEL_HELPER_BUILTIN_SOURCE_URLS.toSet().size,
        )
        assertTrue(NOVEL_HELPER_BUILTIN_SOURCE_URLS.containsAll(expected))
    }

    @Test
    fun missingBuiltinIsInsertedAsPackaged() {
        val packaged = packagedSource()
        assertSame(packaged, prepareBuiltinBookSourceUpdate(packaged, null))
    }

    @Test
    fun managedBuiltinGetsNewRulesWhileKeepingUserState() {
        val packaged = packagedSource().copy(mainJs = "new-js")
        val existing = packagedSource().copy(
            mainJs = "old-js",
            customOrder = 42,
            enabled = false,
            enabledExplore = true,
            respondTime = 1234,
            weight = 7,
        )

        val merged = prepareBuiltinBookSourceUpdate(packaged, existing)!!

        assertEquals("new-js", merged.mainJs)
        assertEquals(42, merged.customOrder)
        assertEquals(false, merged.enabled)
        assertEquals(true, merged.enabledExplore)
        assertEquals(1234, merged.respondTime)
        assertEquals(7, merged.weight)
    }

    @Test
    fun legacyBuiltinEnablesNewlyPackagedExplore() {
        val packaged = packagedSource().copy(
            exploreUrl = "[{\"title\":\"热门榜\",\"url\":\"https://example.com/rank\"}]",
            enabledExplore = true,
        )
        val existing = packagedSource().copy(
            exploreUrl = null,
            enabledExplore = false,
        )

        val merged = prepareBuiltinBookSourceUpdate(packaged, existing)!!

        assertEquals(packaged.exploreUrl, merged.exploreUrl)
        assertEquals(true, merged.enabledExplore)
    }

    @Test
    fun existingExploreDisabledPreferenceIsPreserved() {
        val packaged = packagedSource().copy(
            exploreUrl = "[{\"title\":\"新榜单\",\"url\":\"https://example.com/new-rank\"}]",
            enabledExplore = true,
        )
        val existing = packagedSource().copy(
            exploreUrl = "[{\"title\":\"旧榜单\",\"url\":\"https://example.com/old-rank\"}]",
            enabledExplore = false,
        )

        val merged = prepareBuiltinBookSourceUpdate(packaged, existing)!!

        assertEquals(packaged.exploreUrl, merged.exploreUrl)
        assertEquals(false, merged.enabledExplore)
    }

    @Test
    fun unavailableManagedBuiltinIsDisabledWithoutDeletingIt() {
        val packaged = packagedSource().copy(
            bookSourceUrl = "https://www.hetushu.com",
            bookSourceName = "和图书",
            enabled = false,
            enabledExplore = false,
        )
        val existing = packaged.copy(
            enabled = true,
            enabledExplore = true,
        )

        val merged = prepareBuiltinBookSourceUpdate(packaged, existing)!!

        assertEquals(false, merged.enabled)
        assertEquals(false, merged.enabledExplore)
        assertEquals("https://www.hetushu.com", merged.bookSourceUrl)
    }

    @Test
    fun unavailableSourceCustomizedByUserIsNotManaged() {
        val packaged = packagedSource().copy(
            bookSourceUrl = "https://www.hetushu.com",
            bookSourceName = "和图书",
            enabled = false,
            enabledExplore = false,
        )

        assertNull(
            prepareBuiltinBookSourceUpdate(
                packaged,
                packaged.copy(bookSourceName = "我的和图书"),
            )
        )
    }

    @Test
    fun renamedOrRegroupedSourceIsTreatedAsUserCustomized() {
        val packaged = packagedSource()
        assertNull(
            prepareBuiltinBookSourceUpdate(
                packaged,
                packaged.copy(bookSourceName = "我自己的源")
            )
        )
        assertNull(
            prepareBuiltinBookSourceUpdate(
                packaged,
                packaged.copy(bookSourceGroup = "我的分组")
            )
        )
    }

    @Test
    fun onlyKnownLegacyAutoCoverOnBuiltinSourceIsMigrated() {
        val builtinUrls = setOf("https://www.sto66.com", "https://m.cuoceng.com")

        assertEquals(
            true,
            isLegacyBuiltinAutoCover(
                "https://m.cuoceng.com",
                "https://tsj.youdubook.com/cover/book_cover_123.jpg",
                builtinUrls,
            )
        )
        assertEquals(
            false,
            isLegacyBuiltinAutoCover(
                "https://m.cuoceng.com",
                "https://example.com/my-custom-cover.jpg",
                builtinUrls,
            )
        )
        assertEquals(
            false,
            isLegacyBuiltinAutoCover(
                "https://other-source.example",
                "https://tsj.youdubook.com/cover/book_cover_123.jpg",
                builtinUrls,
            )
        )
    }

    private fun packagedSource() = BookSource(
        bookSourceUrl = "https://example.com",
        bookSourceName = "测试内置源",
        bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
        mainJs = "packaged-js",
    )
}
