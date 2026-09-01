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
import java.net.URI

internal const val NOVEL_HELPER_BUILTIN_SOURCE_GROUP = "网文小助手内置"

internal val NOVEL_HELPER_BUILTIN_SOURCE_URLS = listOf(
    "https://www.sto66.com",
    "https://www.bqquge.org",
    "https://www.bqquge.com",
    "https://www.biquge365.net",
    "https://www.xbiquge345.com",
    "https://www.shudugu.org",
    "https://m.cuoceng.com",
    "https://www.hetushu.com",
    "https://www.biquge432.com",
    "https://www.dingdian100.com",
    "https://www.dingdian678.com",
    "https://www.xbiquge2345.com",
    "https://www.lengsk.com",
)

internal fun prepareBuiltinBookSourceUpdate(
    packaged: BookSource,
    existing: BookSource?,
): BookSource? {
    if (existing == null) return packaged
    if (existing.bookSourceName != packaged.bookSourceName ||
        existing.bookSourceGroup != NOVEL_HELPER_BUILTIN_SOURCE_GROUP
    ) {
        return null
    }
    val enablesNewExplore = existing.exploreUrl.isNullOrBlank() &&
        !packaged.exploreUrl.isNullOrBlank()
    return packaged.copy(
        customOrder = existing.customOrder,
        enabled = existing.enabled,
        enabledExplore = if (enablesNewExplore) packaged.enabledExplore else existing.enabledExplore,
        respondTime = existing.respondTime,
        weight = existing.weight,
    )
}

internal fun isLegacyBuiltinAutoCover(
    origin: String,
    customCoverUrl: String?,
    builtinSourceUrls: Set<String>,
): Boolean {
    if (origin !in builtinSourceUrls || customCoverUrl.isNullOrBlank()) return false
    val host = runCatching { URI(customCoverUrl).host?.lowercase() }.getOrNull()
    return host == "tsj.youdubook.com"
}

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
        val biquge365Js = loadSourceJs("biquge365.js")
        val shuduguJs = loadSourceJs("shudugu.js")
        val cuocengJs = loadSourceJs("cuoceng.js")
        val hetushuJs = loadSourceJs("hetushu.js")
        val biquge432Js = loadSourceJs("biquge432.js")
        val dingdian100Js = loadSourceJs("dingdian100.js")
        val dingdian678Js = loadSourceJs("dingdian678.js")
        val lengskJs = loadSourceJs("lengsk.js")
        val userAgent =
            """{"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36"}"""
        val sto66ExploreUrl =
            """[{"title":"\u70ed\u95e8\u699c","url":"https://www.sto66.com/ranking.html"},{"title":"\u5b8c\u672c\u699c","url":"https://www.sto66.com/full.html"}]"""
        val bqqugeExploreUrl =
            """[{"title":"\u70ed\u95e8\u699c","url":"https://www.bqquge.org/paihang"},{"title":"\u8fde\u8f7d\u699c","url":"https://www.bqquge.org/lianzai"},{"title":"\u5b8c\u672c\u699c","url":"https://www.bqquge.org/wanjie"},{"title":"\u7384\u5e7b","url":"https://www.bqquge.org/xuanhuan"},{"title":"\u4ed9\u4fa0","url":"https://www.bqquge.org/xianxia"},{"title":"\u90fd\u5e02","url":"https://www.bqquge.org/dushi"}]"""
        val shuduguExploreUrl =
            """[{"title":"热门榜","url":"https://www.shudugu.org/paihang/"},{"title":"完本榜","url":"https://www.shudugu.org/wanjie/"},{"title":"最新更新","url":"https://www.shudugu.org/zuixin/"},{"title":"玄幻","url":"https://www.shudugu.org/xuanhuan/"},{"title":"仙侠","url":"https://www.shudugu.org/xianxia/"},{"title":"都市","url":"https://www.shudugu.org/dushi/"},{"title":"历史","url":"https://www.shudugu.org/lishi/"},{"title":"科幻","url":"https://www.shudugu.org/kehuan/"}]"""
        val hetushuExploreUrl =
            """[{"title":"\u70ed\u95e8\u699c","url":"https://m.hetushu.com/top/index.php"},{"title":"\u5b8c\u672c\u699c","url":"https://www.hetushu.com/book/index.php?state=2"},{"title":"\u7384\u5e7b","url":"https://m.hetushu.com/book/list.php?type=%E7%8E%84%E5%B9%BB%E5%B0%8F%E8%AF%B4"}]"""
        val cuocengExploreUrl =
            """[{"title":"\u70ed\u95e8\u699c","url":"https://m.cuoceng.com/book/ranking.html"},{"title":"\u5b8c\u672c\u699c","url":"https://m.cuoceng.com/book/finish.html"},{"title":"\u7384\u5e7b","url":"https://m.cuoceng.com/book/category/catalog.html"}]"""
        val dingdian100ExploreUrl =
            """[{"title":"\u70ed\u95e8\u699c","url":"https://www.dingdian100.com/"},{"title":"\u5b8c\u672c\u699c","url":"https://www.dingdian100.com/full/"},{"title":"\u7384\u5e7b","url":"https://www.dingdian100.com/sort/1_1/"},{"title":"\u4ed9\u4fa0","url":"https://www.dingdian100.com/sort/2_1/"},{"title":"\u90fd\u5e02","url":"https://www.dingdian100.com/sort/3_1/"}]"""
        val hetushuHeader =
            """{"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36","Referer":"https://www.hetushu.com/"}"""
        listOf(
            BookSource(
                bookSourceUrl = "https://www.sto66.com",
                bookSourceName = "思兔阅读",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = false,
                header = userAgent,
                exploreUrl = sto66ExploreUrl,
                bookSourceComment = "网文小助手内置公开网页源。当前无需登录，按 sto66.com 页面结构解析。",
                mainJs = sto66Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.bqquge.org",
                bookSourceName = "笔趣阁",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = false,
                header = userAgent,
                exploreUrl = bqqugeExploreUrl,
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
                bookSourceUrl = "https://www.biquge365.net",
                bookSourceName = "笔趣阁365",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。按 biquge365.net 当前公开页面结构解析。",
                mainJs = biquge365Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.xbiquge345.com",
                bookSourceName = "新笔趣阁345",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。与笔趣阁365使用同模板并带目录回退。",
                mainJs = biquge365Js
                    .replace("https://www.biquge365.net", "https://www.xbiquge345.com")
                    .replace("笔趣阁365", "新笔趣阁345"),
            ),
            BookSource(
                bookSourceUrl = "https://www.shudugu.org",
                bookSourceName = "速读谷",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = false,
                header = userAgent,
                exploreUrl = shuduguExploreUrl,
                bookSourceComment = "网文小助手内置公开网页源。按 shudugu.org 当前页面结构解析。",
                mainJs = shuduguJs,
            ),
            BookSource(
                bookSourceUrl = "https://m.cuoceng.com",
                bookSourceName = "错层小说",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = false,
                header = userAgent,
                exploreUrl = cuocengExploreUrl,
                bookSourceComment = "网文小助手内置公开网页源。按 m.cuoceng.com 当前移动页面结构解析。",
                mainJs = cuocengJs,
            ),
            BookSource(
                bookSourceUrl = "https://www.hetushu.com",
                bookSourceName = "和图书",
                bookSourceGroup = "网文小助手内置",
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = true,
                header = hetushuHeader,
                exploreUrl = hetushuExploreUrl,
                bookSourceComment = "网文小助手内置公开网页源。使用普通 Cookie 会话访问公开搜索与阅读页面。",
                mainJs = hetushuJs,
            ),
            BookSource(
                bookSourceUrl = "https://www.biquge432.com",
                bookSourceName = "笔趣阁432",
                bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。搜索有频率限制，避免连续重复搜索。",
                mainJs = biquge432Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.dingdian100.com",
                bookSourceName = "顶点100",
                bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
                bookSourceType = 0,
                enabled = true,
                enabledExplore = true,
                enabledCookieJar = false,
                header = userAgent,
                exploreUrl = dingdian100ExploreUrl,
                bookSourceComment = "网文小助手内置公开网页源。目录使用 newbook 分页入口。",
                mainJs = dingdian100Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.dingdian678.com",
                bookSourceName = "顶点678",
                bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。目录直接解析书籍详情页。",
                mainJs = dingdian678Js,
            ),
            BookSource(
                bookSourceUrl = "https://www.xbiquge2345.com",
                bookSourceName = "新笔趣阁2345",
                bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。与笔趣阁432使用同族页面规则。",
                mainJs = biquge432Js
                    .replace("https://www.biquge432.com", "https://www.xbiquge2345.com")
                    .replace("笔趣阁432", "新笔趣阁2345"),
            ),
            BookSource(
                bookSourceUrl = "https://www.lengsk.com",
                bookSourceName = "冷书库",
                bookSourceGroup = NOVEL_HELPER_BUILTIN_SOURCE_GROUP,
                bookSourceType = 0,
                enabled = true,
                enabledExplore = false,
                enabledCookieJar = false,
                header = userAgent,
                bookSourceComment = "网文小助手内置公开网页源。目录与正文分页自动合并。",
                mainJs = lengskJs,
            ),
        ).also { sources ->
            check(sources.map { it.bookSourceUrl } == NOVEL_HELPER_BUILTIN_SOURCE_URLS) {
                "内置书源清单与实际配置不一致"
            }
        }
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

        val sourcesToUpsert = bookSources.mapNotNull { packaged ->
            prepareBuiltinBookSourceUpdate(
                packaged,
                appDb.bookSourceDao.getBookSource(packaged.bookSourceUrl),
            )
        }
        if (sourcesToUpsert.isNotEmpty()) {
            SourceHelp.insertBookSource(*sourcesToUpsert.toTypedArray())
        }

        val builtinSourceUrls = bookSources.mapTo(hashSetOf()) { it.bookSourceUrl }
        appDb.bookDao.all.forEach { book ->
            if (isLegacyBuiltinAutoCover(book.origin, book.customCoverUrl, builtinSourceUrls)) {
                appDb.bookDao.clearCoverOverridesIfUnchanged(
                    book.bookUrl,
                    book.customCoverUrl,
                    book.persistedCoverUrl,
                )
            }
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
