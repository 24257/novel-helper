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
        )
        assertEquals(13, NOVEL_HELPER_BUILTIN_SOURCE_URLS.size)
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
