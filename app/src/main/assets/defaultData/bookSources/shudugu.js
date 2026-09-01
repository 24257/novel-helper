var config = {
    bookSourceUrl: "https://www.shudugu.org",
    bookSourceName: "速读谷",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。按 shudugu.org 当前页面结构适配。",
    exploreUrl: [
        {title: "热门榜", url: "https://www.shudugu.org/paihang/"},
        {title: "完本榜", url: "https://www.shudugu.org/wanjie/"},
        {title: "最新更新", url: "https://www.shudugu.org/zuixin/"},
        {title: "玄幻", url: "https://www.shudugu.org/xuanhuan/"},
        {title: "仙侠", url: "https://www.shudugu.org/xianxia/"},
        {title: "都市", url: "https://www.shudugu.org/dushi/"},
        {title: "历史", url: "https://www.shudugu.org/lishi/"},
        {title: "科幻", url: "https://www.shudugu.org/kehuan/"}
    ],
    lastUpdateTime: 1788220800000
};

var Jsoup = org.jsoup.Jsoup;

function safeString(value) { return value == null ? "" : String(value); }
function trimText(value) {
    return safeString(value).replace(/\u00a0/g, " ").replace(/\u3000/g, " ")
        .replace(/[\t\r\n]+/g, " ").replace(/\s{2,}/g, " ").trim();
}
function requestHtml(url) {
    var html = safeString(java.ajax(safeString(url), 20000));
    if (!html) throw "速读谷请求失败: " + url;
    return html;
}
function resolveUrl(base, href) {
    var value = trimText(href);
    if (!value) return "";
    if (/^https?:\/\//i.test(value)) return value;
    if (/^\/\//.test(value)) return "https:" + value;
    if (value.charAt(0) === "/") return config.bookSourceUrl + value;
    var cleanBase = safeString(base).split("#")[0].split("?")[0];
    var slash = cleanBase.lastIndexOf("/");
    return slash < 0 ? value : cleanBase.substring(0, slash + 1) + value;
}
function imageUrl(base, element) {
    if (element == null) return "";
    var attrs = ["src", "data-src", "data-original", "data-lazy-src", "data-url"];
    for (var i = 0; i < attrs.length; i++) {
        var value = trimText(element.attr(attrs[i]));
        if (value && value !== "#" && !/^data:/i.test(value)) return resolveUrl(base, value);
    }
    return "";
}
function firstText(element, selector) {
    if (element == null) return "";
    var node = element.selectFirst(selector);
    return node == null ? "" : trimText(node.text());
}
function parseAuthor(text) { return trimText(text).replace(/^作者\s*[:：]?\s*/, ""); }

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword) return [];
    if (Number(page) > 1) return [];
    var url = config.bookSourceUrl + "/i/sor.aspx?key=" + java.encodeURI(keyword, "UTF-8");
    var doc = Jsoup.parse(requestHtml(url), url);
    var rows = doc.select("div.item");
    var books = [];
    var seen = {};
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h3 a[href]");
        if (link == null) continue;
        var bookUrl = resolveUrl(url, link.attr("href"));
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl]) continue;
        seen[bookUrl] = true;
        var cover = row.selectFirst("a[href] img");
        books.push({
            name: name,
            author: parseAuthor(firstText(row, "p a[href*=/zuozhe/]")),
            coverUrl: imageUrl(url, cover),
            latestChapterTitle: firstText(row, "ul li a[href]"),
            bookUrl: bookUrl,
            tocUrl: bookUrl
        });
    }
    return books;
}

function explore(url, page) {
    var target = trimText(url);
    if (!target) return [];
    target = target.replace(/\/+$/, "");
    var pageNo = Number(page) || 1;
    if (pageNo > 1) target += "/" + pageNo + ".html";
    else target += "/";

    var doc = Jsoup.parse(requestHtml(target), target);
    var books = [];
    var seen = {};

    function addBook(link, container) {
        if (link == null) return;
        var bookUrl = resolveUrl(target, link.attr("href"));
        if (!/^https?:\/\/(www\.)?shudugu\.org\/\d+\/?$/i.test(bookUrl)) return;
        var name = trimText(link.text());
        if (!name || seen[bookUrl]) return;
        seen[bookUrl] = true;

        var scope = container;
        if (scope == null) scope = link.parent();
        for (var depth = 0; scope != null && depth < 5; depth++) {
            var scopeText = trimText(scope.text());
            var className = trimText(scope.className());
            if (scopeText.indexOf("作者：") >= 0 || scopeText.indexOf("作者:") >= 0 ||
                className.indexOf("item") >= 0) break;
            var parent = scope.parent();
            if (parent == null) break;
            scope = parent;
        }

        var author = parseAuthor(firstText(scope, "a[href*=/zuozhe/]"));
        if (!author && scope != null) {
            var authorMatch = trimText(scope.text()).match(/作者\s*[:：]\s*([^\s]+)/);
            if (authorMatch) author = trimText(authorMatch[1]);
        }
        var cover = scope == null ? null : scope.selectFirst("img");
        var latest = firstText(scope, "a[href$=.html]");
        books.push({
            name: name,
            author: author,
            coverUrl: imageUrl(target, cover),
            latestChapterTitle: latest,
            bookUrl: bookUrl,
            tocUrl: bookUrl
        });
    }

    var rows = doc.select("div.item");
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h3 a[href]");
        if (link == null) link = row.selectFirst("a[href]");
        addBook(link, row);
    }

    var links = doc.select("a[href]");
    for (var j = 0; j < links.size(); j++) {
        addBook(links.get(j), null);
    }
    return books.filter(function(book) {
        return trimText(book.coverUrl).length > 0;
    });
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) throw "速读谷书籍地址为空";
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var info = doc.selectFirst("div.itemtxt");
    var container = info == null ? null : info.parent();
    var cover = container == null ? null : container.selectFirst("img");
    var kind = [];
    if (info != null) {
        var spans = info.select("p span");
        for (var i = 0; i < spans.size(); i++) {
            var value = trimText(spans.get(i).text());
            if (value) kind.push(value);
        }
    }
    return {
        name: firstText(info, "h1 a[href]") || trimText(book.name),
        author: parseAuthor(firstText(info, "p a[href*=/zuozhe/]")) || trimText(book.author),
        intro: firstText(doc, "div.des"),
        coverUrl: imageUrl(bookUrl, cover) || trimText(book.coverUrl),
        kind: kind.join(","),
        latestChapterTitle: firstText(info, "ul li a[href]"),
        tocUrl: bookUrl
    };
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || trimText(book.bookUrl);
    if (!tocUrl) throw "速读谷目录地址为空";
    var doc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    var links = doc.select("#list.dir a[href]");
    var chapters = [];
    var seen = {};
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i);
        var url = resolveUrl(tocUrl, link.attr("href"));
        var title = trimText(link.text());
        if (!url || !title || seen[url]) continue;
        seen[url] = true;
        chapters.push({title: title, url: url});
    }
    return chapters;
}

function appendContent(doc, pieces) {
    var content = doc.selectFirst("div.con");
    if (content == null) return;
    var ps = content.select("p");
    for (var i = 0; i < ps.size(); i++) {
        var text = trimText(ps.get(i).text());
        if (text && !/^记住我们网/.test(text)) pieces.push(text);
    }
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = trimText(chapter.url);
    if (!chapterUrl) throw "速读谷章节地址为空";
    var stem = chapterUrl.replace(/\.html(?:[?#].*)?$/i, "").replace(/-\d+$/, "");
    var currentUrl = chapterUrl;
    var visited = {};
    var pieces = [];
    for (var pageNo = 0; pageNo < 20 && currentUrl && !visited[currentUrl]; pageNo++) {
        visited[currentUrl] = true;
        var doc = Jsoup.parse(requestHtml(currentUrl), currentUrl);
        appendContent(doc, pieces);
        var links = doc.select("div.prenext a[href]");
        var nextPage = "";
        for (var i = 0; i < links.size(); i++) {
            var link = links.get(i);
            if (trimText(link.text()).indexOf("下一页") >= 0) {
                var candidate = resolveUrl(currentUrl, link.attr("href"));
                if (candidate.indexOf(stem + "-") === 0) nextPage = candidate;
                break;
            }
        }
        currentUrl = nextPage;
    }
    if (pieces.length === 0) throw "速读谷正文节点不存在: " + chapterUrl;
    return pieces.join("\n");
}
