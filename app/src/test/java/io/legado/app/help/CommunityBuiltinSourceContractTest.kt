package io.legado.app.help

import io.legado.app.data.entities.BookSource
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CommunityBuiltinSourceContractTest {

    @Test
    fun pbCommunityBundleContainsExactlyTheFourValidatedSources() {
        val sources = GSON.fromJsonArray<BookSource>(
            projectFile("src/main/assets/defaultData/bookSources/community_pb.json").readText()
        ).getOrThrow()

        assertEquals(
            listOf(
                "https://www.00shu.la/",
                "https://wujixsw.info/",
                "http://www.gdbzkz.com/",
                "http://www.xinbqg.org/",
            ),
            sources.map { it.bookSourceUrl },
        )
        assertEquals(listOf("零零小说", "无极小说", "鬼吹灯", "笔趣阁㉒"), sources.map { it.bookSourceName })
        sources.forEach { source ->
            assertTrue("${source.bookSourceName} must be enabled", source.enabled)
            assertTrue("${source.bookSourceName} must search without login", source.loginUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search", !source.searchUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search rules", source.ruleSearch != null)
            assertTrue("${source.bookSourceName} needs book-info rules", source.ruleBookInfo != null)
            assertTrue("${source.bookSourceName} needs toc rules", source.ruleToc != null)
            assertTrue("${source.bookSourceName} needs content rules", source.ruleContent != null)
        }
    }

    @Test
    fun extraCommunityBundleContainsExactlyTheThreeValidatedSources() {
        val sources = GSON.fromJsonArray<BookSource>(
            projectFile("src/main/assets/defaultData/bookSources/community_extra.json").readText()
        ).getOrThrow()

        assertEquals(
            listOf(
                "https://www.yeban360.com",
                "https://zhongtianwen.cn",
                "http://www.feisuwx.org",
            ),
            sources.map { it.bookSourceUrl },
        )
        assertEquals(listOf("夜伴书屋", "种田小说", "飞速小说网"), sources.map { it.bookSourceName })
        sources.forEach { source ->
            assertTrue("${source.bookSourceName} must be enabled", source.enabled)
            assertTrue("${source.bookSourceName} must search without login", source.loginUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search", !source.searchUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search rules", source.ruleSearch != null)
            assertTrue("${source.bookSourceName} needs book-info rules", source.ruleBookInfo != null)
            assertTrue("${source.bookSourceName} needs toc rules", source.ruleToc != null)
            assertTrue("${source.bookSourceName} needs content rules", source.ruleContent != null)
        }
    }

    @Test
    fun coverCommunityBundleContainsExactlyTenStrictlyValidatedSources() {
        val sources = GSON.fromJsonArray<BookSource>(
            projectFile("src/main/assets/defaultData/bookSources/community_cover.json").readText()
        ).getOrThrow()

        assertEquals(
            listOf(
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
            ),
            sources.map { it.bookSourceUrl },
        )
        assertEquals(
            listOf(
                "若雨中文",
                "爱下文学",
                "野天链小说",
                "笔趣阁A17",
                "笔趣阁㉘",
                "14oz笔趣阁",
                "去读读小说网",
                "华博文都",
                "全本小说5300",
                "就爱文学",
            ),
            sources.map { it.bookSourceName },
        )
        sources.forEach { source ->
            assertTrue("${source.bookSourceName} must be enabled", source.enabled)
            assertTrue("${source.bookSourceName} must not require login", source.loginUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search", !source.searchUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search-result covers", !source.ruleSearch?.coverUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs detail covers", !source.ruleBookInfo?.coverUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs toc rules", source.ruleToc != null)
            assertTrue("${source.bookSourceName} needs content rules", source.ruleContent != null)
            val coverRules = "${source.ruleSearch?.coverUrl}\n${source.ruleBookInfo?.coverUrl}".lowercase()
            assertTrue("${source.bookSourceName} must not use nocover", !coverRules.contains("nocover"))
            assertTrue("${source.bookSourceName} must not use a default cover", !coverRules.contains("default"))
        }
        sources.filter { it.bookSourceName in setOf("14oz笔趣阁", "去读读小说网") }.forEach { source ->
            val content = source.ruleContent?.content.orEmpty()
            val nextContent = source.ruleContent?.nextContentUrl.orEmpty()
            assertTrue("${source.bookSourceName} must decrypt AES content", content.contains("CryptoJS.AES.decrypt"))
            assertTrue("${source.bookSourceName} must use ZeroPadding", content.contains("CryptoJS.pad.ZeroPadding"))
            assertTrue("${source.bookSourceName} must stop pagination before next chapter", nextContent.contains("cm[1]===nm[1]"))
            assertTrue("${source.bookSourceName} needs paginated toc", source.ruleToc?.nextTocUrl == "text.下一页@href")
        }
    }

    @Test
    fun strictCommunityBundleContainsExactlySevenIndependentValidatedSources() {
        val sources = GSON.fromJsonArray<BookSource>(
            projectFile("src/main/assets/defaultData/bookSources/community_strict7.json").readText()
        ).getOrThrow()

        assertEquals(
            listOf(
                "http://wap.qiqixs.info",
                "http://www.23uswx.la",
                "https://www.shenhuazhihou.com",
                "https://www.zhizhuxs.com",
                "https://m.kudushu.org",
                "http://wap.wangshuge.la",
                "http://www.qudushu.com",
            ),
            sources.map { it.bookSourceUrl },
        )
        assertEquals(
            listOf("平板电子书网", "顶点小说", "神话之后", "蜘蛛小说网", "苦读书", "望书阁网", "去读书"),
            sources.map { it.bookSourceName },
        )
        sources.forEach { source ->
            assertTrue("${source.bookSourceName} must be enabled", source.enabled)
            assertTrue("${source.bookSourceName} must not require login", source.loginUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search", !source.searchUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs search rules", source.ruleSearch != null)
            assertTrue("${source.bookSourceName} needs search-result covers", !source.ruleSearch?.coverUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs detail covers", !source.ruleBookInfo?.coverUrl.isNullOrBlank())
            assertTrue("${source.bookSourceName} needs toc rules", source.ruleToc != null)
            assertTrue("${source.bookSourceName} needs content rules", source.ruleContent != null)
            val coverRules = "${source.ruleSearch?.coverUrl}\n${source.ruleBookInfo?.coverUrl}".lowercase()
            listOf("nocover", "no-cover", "default", "placeholder", "logo").forEach { marker ->
                assertTrue("${source.bookSourceName} must not use $marker as its cover", !coverRules.contains(marker))
            }
        }
        sources.filterNot { it.bookSourceName == "平板电子书网" }.forEach { source ->
            assertTrue(
                "${source.bookSourceName} detail cover should be deterministic og:image",
                source.ruleBookInfo?.coverUrl.orEmpty().contains("og:image"),
            )
        }
    }

    @Test
    fun defaultDataLoadsCommunityBundleAsManagedBuiltins() {
        val defaultData = projectFile("src/main/java/io/legado/app/help/DefaultData.kt").readText()
        assertTrue(defaultData.contains("loadSourceJson(\"community_pb.json\")"))
        assertTrue(defaultData.contains("loadSourceJson(\"community_extra.json\")"))
        assertTrue(defaultData.contains("loadSourceJson(\"community_cover.json\")"))
        assertTrue(defaultData.contains("loadSourceJson(\"community_strict7.json\")"))
        assertTrue(defaultData.contains("bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP"))
        assertTrue(defaultData.contains("PB-pobing/pobing"))
        assertTrue(defaultData.contains("Legado 社区全量书源池"))
        assertTrue(defaultData.contains("玄卷内置严格实测社区源"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
