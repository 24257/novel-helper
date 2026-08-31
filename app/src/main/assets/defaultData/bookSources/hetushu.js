var config = {
    bookSourceUrl: "https://www.hetushu.com",
    bookSourceName: "和图书",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。使用站点普通 Cookie 会话访问公开搜索与阅读页面。",
    enabledCookieJar: true,
    exploreUrl: [],
    lastUpdateTime: 1788159300000
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
    if (!html) throw "和图书请求失败: " + url;
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

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword || Number(page) > 1) return [];
    requestHtml(config.bookSourceUrl + "/");
    var url = config.bookSourceUrl + "/search/?keyword=" + java.encodeURI(keyword, "UTF-8");
    var doc = Jsoup.parse(requestHtml(url), url);
    var rows = doc.select("dl#body dd");
    if (rows.isEmpty()) rows = doc.select("dl.list dd");
    var books = [];
    var seen = {};
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h4 a[href*=\/book\/]");
        if (link == null) continue;
        var bookUrl = resolveUrl(url, link.attr("href"));
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl] || !/\/book\/\d+\/index\.html(?:[?#].*)?$/i.test(bookUrl)) continue;
        seen[bookUrl] = true;
        var author = firstText(row, "h4 span")
            .replace(/^\s*\/\s*/, "")
            .replace(/\s*\/\s*$/, "")
            .trim();
        books.push({
            name: name,
            author: author,
            intro: firstText(row, "div.intro"),
            coverUrl: imageUrl(url, row.selectFirst("img")),
            bookUrl: bookUrl,
            tocUrl: bookUrl
        });
    }
    return books;
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) throw "和图书书籍地址为空";
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var name = firstText(doc, "#left h3 a[href]") || firstText(doc, "h1") || trimText(book.name);
    var author = firstText(doc, "#left div.author a[href]") || trimText(book.author);
    var cover = doc.selectFirst("img[src*=cover.pic]");
    var intro = firstText(doc, "div.intro") || firstText(doc, "div.book_info div.intro");
    if (!intro) {
        var description = doc.selectFirst("meta[name=description]");
        intro = description == null ? "" : trimText(description.attr("content"));
    }
    return {
        name: name,
        author: author,
        intro: intro,
        coverUrl: imageUrl(bookUrl, cover) || trimText(book.coverUrl),
        tocUrl: bookUrl
    };
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || trimText(book.bookUrl);
    if (!tocUrl) throw "和图书目录地址为空";
    var match = tocUrl.match(/\/book\/(\d+)\/index\.html/i);
    if (!match) throw "和图书无法识别书籍编号";
    var bookId = match[1];
    var doc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    var links = doc.select("a[href*=\/book\/]");
    var pattern = new RegExp("/book/" + bookId + "/\\d+\\.html(?:[?#].*)?$", "i");
    var chapters = [];
    var seen = {};
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i);
        var url = resolveUrl(tocUrl, link.attr("href"));
        var title = trimText(link.attr("title")) || trimText(link.text());
        if (!url || !title || !pattern.test(url) || seen[url]) continue;
        seen[url] = true;
        chapters.push({title: title, url: url});
    }
    if (chapters.length === 0) throw "和图书目录为空: " + tocUrl;
    return chapters;
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = trimText(chapter.url);
    if (!chapterUrl) throw "和图书章节地址为空";
    var doc = Jsoup.parse(requestHtml(chapterUrl), chapterUrl);
    var content = doc.selectFirst("#content");
    if (content == null) throw "和图书正文节点不存在: " + chapterUrl;
    content.select(".mask,h2,var,q,kbd,samp,script,style,iframe,form").remove();
    var blocks = content.children();
    var pieces = [];
    for (var i = 0; i < blocks.size(); i++) {
        var text = trimText(blocks.get(i).text());
        if (text) pieces.push(text);
    }
    if (pieces.length === 0) {
        var fallback = trimText(content.text());
        if (fallback) pieces.push(fallback);
    }
    if (pieces.length === 0) throw "和图书正文为空: " + chapterUrl;
    return pieces.join("\n");
}
