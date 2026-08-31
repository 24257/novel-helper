package io.legado.app.help

import io.legado.app.constant.AppConst
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.DictRule
import io.legado.app.data.entities.HttpTTS
import io.legado.app.data.entities.KeyboardAssist
import io.legado.app.data.entities.RssSource
import io.legado.app.data.entities.TxtTocRule
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.help.config.ThemeConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.source.SourceHelp
import io.legado.app.help.source.clearSharedGlobalState
import io.legado.app.model.BookCover
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.printOnDebug
import splitties.init.appCtx
import java.io.File

object DefaultData {

    fun upVersion() {
        if (LocalConfig.needUpBookSources) {
            Coroutine.async {
                importDefaultBookSources()
            }.onError {
                it.printOnDebug()
            }
        }
        if (LocalConfig.versionCode < AppConst.appInfo.versionCode) {
            Coroutine.async {
                if (LocalConfig.needUpHttpTTS) {
                    importDefaultHttpTTS()
                }
                if (LocalConfig.needUpTxtTocRule) {
                    importDefaultTocRules()
                }
                if (LocalConfig.needUpRssSources) {
                    importDefaultRssSources()
                }
                if (LocalConfig.needUpDictRule) {
                    importDefaultDictRules()
                }
            }.onError {
                it.printOnDebug()
            }
        }
    }

    val bookSources: List<BookSource> by lazy {
        fun loadSourceJs(fileName: String): String = String(
            appCtx.assets.open(
                "defaultData${File.separator}bookSources${File.separator}$fileName"
            ).readBytes(),
            Charsets.UTF_8,
        )

        val sto66Js = loadSourceJs("sto66.js")
        val bqqugeJs = loadSourceJs("bqquge.js")
        val shuduguJs = loadSourceJs("shudugu.js")
        val cuocengJs = loadSourceJs("cuoceng.js")
        val userAgent =
            """{"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36"}"""
        listOf(
            BookSource(
                bookSourceUrl = "https://www.sto66.com",
                bookSourceName = "思兔阅读",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。当前无需登录，按 sto66.com 页面结构解析。",
                mainJs = sto66Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.bqquge.org",
                bookSourceName = "笔趣阁",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。按 bqquge.org 当前页面结构解析。",
                mainJs = bqqugeJs,
            ),
            BookSource(
                bookSourceUrl = "https://www.bqquge.com",
                bookSourceName = "笔趣阁镜像",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置备用镜像。与 bqquge.org 使用同结构规则。",
                mainJs = bqqugeJs.replace("https://www.bqquge.org", "https://www.bqquge.com"),
            ),
            BookSource(
                bookSourceUrl = "https://www.shudugu.org",
                bookSourceName = "速读谷",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。按 shudugu.org 当前页面结构解析。",
                mainJs = shuduguJs,
            ),
            BookSource(
                bookSourceUrl = "https://m.cuoceng.com",
                bookSourceName = "错层小说",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。按 m.cuoceng.com 当前移动页面结构解析。",
                mainJs = cuocengJs,
            ),
        )
    }

    val httpTTS: List<HttpTTS> by lazy {
        val json =
            String(
                appCtx.assets.open("defaultData${File.separator}httpTTS.json")
                    .readBytes()
            )
        HttpTTS.fromJsonArray(json).getOrElse {
            emptyList()
        }
    }

    val readConfigs: List<ReadBookConfig.Config> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}${ReadBookConfig.configFileName}")
                .readBytes()
        )
        GSON.fromJsonArray<ReadBookConfig.Config>(json).getOrNull()
            ?: emptyList()
    }

    val txtTocRules: List<TxtTocRule> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}txtTocRule.json")
                .readBytes()
        )
        GSON.fromJsonArray<TxtTocRule>(json).getOrNull() ?: emptyList()
    }

    val themeConfigs: List<ThemeConfig.Config> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}${ThemeConfig.configFileName}")
                .readBytes()
        )
        GSON.fromJsonArray<ThemeConfig.Config>(json).getOrNull() ?: emptyList()
    }

    val rssSources: List<RssSource> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}rssSources.json")
                .readBytes()
        )
        GSON.fromJsonArray<RssSource>(json).getOrDefault(emptyList())
    }

    val coverRule: BookCover.CoverRule by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}coverRule.json")
                .readBytes()
        )
        GSON.fromJsonObject<BookCover.CoverRule>(json).getOrThrow()
    }

    val dictRules: List<DictRule> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}dictRules.json")
                .readBytes()
        )
        GSON.fromJsonArray<DictRule>(json).getOrThrow()
    }

    val keyboardAssists: List<KeyboardAssist> by lazy {
        val json = String(
            appCtx.assets.open("defaultData${File.separator}keyboardAssists.json")
                .readBytes()
        )
        GSON.fromJsonArray<KeyboardAssist>(json).getOrThrow()
    }

    fun importDefaultBookSources() {
        listOf(
            "https://www.piaotia.com" to "飘天文学",
            "https://www.quanben.io" to "全本小说网",
        ).forEach { (url, name) ->
            appDb.bookSourceDao.getBookSource(url)
                ?.takeIf {
                    it.bookSourceName == name &&
                        it.bookSourceGroup == "网文小助手内置"
                }
                ?.let { SourceHelp.deleteBookSource(it.bookSourceUrl) }
        }

        val missingSources = bookSources.filter {
            appDb.bookSourceDao.getBookSource(it.bookSourceUrl) == null
        }
        if (missingSources.isNotEmpty()) {
            SourceHelp.insertBookSource(*missingSources.toTypedArray())
        }
    }

    fun importDefaultHttpTTS() {
        appDb.httpTTSDao.all
            .filter { it.id < 0 }
            .forEach { it.clearSharedGlobalState() }
        appDb.httpTTSDao.deleteDefault()
        appDb.httpTTSDao.insert(*httpTTS.toTypedArray())
    }

    fun importDefaultTocRules() {
        appDb.txtTocRuleDao.deleteDefault()
        appDb.txtTocRuleDao.insert(*txtTocRules.toTypedArray())
    }

    fun importDefaultRssSources() {
        appDb.rssSourceDao.all
            .filter { it.sourceGroup == "legado" }
            .forEach { it.clearSharedGlobalState() }
        appDb.rssSourceDao.deleteDefault()
        appDb.rssSourceDao.insert(*rssSources.toTypedArray())
    }

    fun importDefaultDictRules() {
        appDb.dictRuleDao.insert(*dictRules.toTypedArray())
    }

}
