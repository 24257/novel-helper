var config = {
    bookSourceUrl: "https://www.bqquge.org",
    bookSourceName: "笔趣阁",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。按 bqquge.org 当前页面结构适配。",
    exploreUrl: [
        {title: "\u70ed\u95e8\u699c", url: "https://www.bqquge.org/paihang"},
        {title: "\u8fde\u8f7d\u699c", url: "https://www.bqquge.org/lianzai"},
        {title: "\u5b8c\u672c\u699c", url: "https://www.bqquge.org/wanjie"},
        {title: "\u7384\u5e7b", url: "https://www.bqquge.org/xuanhuan"},
        {title: "\u4ed9\u4fa0", url: "https://www.bqquge.org/xianxia"},
        {title: "\u90fd\u5e02", url: "https://www.bqquge.org/dushi"}
    ],
    lastUpdateTime: 1788282000000
};

var Jsoup = org.jsoup.Jsoup;

function safeString(value) {
    return value == null ? "" : String(value);
}

function trimText(value) {
    return safeString(value)
        .replace(/\u00a0/g, " ")
        .replace(/\u3000/g, " ")
        .replace(/[\t\r\n]+/g, " ")
        .replace(/\s{2,}/g, " ")
        .trim();
}

function requestHtml(url) {
    var html = safeString(java.ajax(safeString(url), 20000));
    if (!html) throw "笔趣阁请求失败: " + url;
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

function parseAuthor(text) {
    return trimText(text).replace(/^作者\s*[:：]?\s*/, "");
}

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword) return [];
    if (Number(page) > 1) return [];
    var url = config.bookSourceUrl + "/so/" + java.encodeURI(keyword, "UTF-8");
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
    var pageUrl = trimText(url);
    var pageNo = Number(page) > 0 ? Number(page) : 1;
    if (!pageUrl) return [];
    if (pageNo > 1) pageUrl = pageUrl.replace(/\/$/, "") + "/" + pageNo;
    var doc = Jsoup.parse(requestHtml(pageUrl), pageUrl);
    var books = [];
    var seen = {};
    var rows = doc.select("div.item");
    for (var i = 0; i < rows.size() && books.length < 20; i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h3 a[href]");
        if (link == null) continue;
        var bookUrl = resolveUrl(pageUrl, link.attr("href")).replace(/\/$/, "");
        var name = trimText(link.text()).replace(/^\d+\s*/, "");
        var coverUrl = imageUrl(pageUrl, row.selectFirst("a[href] img"));
        if (!/^https?:\/\/(?:www\.)?bqquge\.org\/\d+$/i.test(bookUrl) || !name || !coverUrl || seen[bookUrl]) continue;
        seen[bookUrl] = true;
        var authorText = firstText(row, "p:contains(作者)");
        var kindNodes = row.select("p span");
        var kindParts = [];
        for (var k = 0; k < kindNodes.size(); k++) {
            var kind = trimText(kindNodes.get(k).text());
            if (kind) kindParts.push(kind);
        }
        books.push({
            name: name,
            author: parseAuthor(authorText),
            coverUrl: coverUrl,
            kind: kindParts.join(","),
            latestChapterTitle: firstText(row, "ul li a[href]"),
            bookUrl: bookUrl,
            tocUrl: bookUrl
        });
    }
    return books;
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) throw "笔趣阁书籍地址为空";
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var detail = doc.selectFirst("div.bookdetail");
    var cover = detail == null ? null : detail.selectFirst("img");
    var ogImage = doc.selectFirst('meta[property="og:image"]');
    var kindParts = [];
    if (detail != null) {
        var ps = detail.select("div.booktxt p");
        for (var i = 0; i < ps.size(); i++) {
            var text = trimText(ps.get(i).text());
            if (/^(类别|状态)[:：]/.test(text)) kindParts.push(text.replace(/^[^:：]+[:：]\s*/, ""));
        }
    }
    return {
        name: firstText(detail, "div.booktxt h1") || trimText(book.name),
        author: parseAuthor(firstText(detail, "div.booktxt p a[href*=/zuozhe/]")) || trimText(book.author),
        intro: firstText(doc, "div.des"),
        coverUrl: imageUrl(bookUrl, cover) || (ogImage == null ? "" : trimText(ogImage.attr("content"))) || trimText(book.coverUrl),
        kind: kindParts.join(","),
        latestChapterTitle: firstText(doc, "div.newest h3 a[href]"),
        tocUrl: bookUrl
    };
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || trimText(book.bookUrl);
    if (!tocUrl) throw "笔趣阁目录地址为空";
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
    if (!chapterUrl) throw "笔趣阁章节地址为空";
    var stem = chapterUrl.replace(/-\d+$/, "");
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
    if (pieces.length === 0) throw "笔趣阁正文节点不存在: " + chapterUrl;
    return pieces.join("\n");
}
