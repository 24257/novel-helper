var config = {
    bookSourceUrl: "https://www.biquge365.net",
    bookSourceName: "笔趣阁365",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。按 biquge365.net 当前公开页面结构适配。",
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
    if (!html) throw config.bookSourceName + "请求失败: " + url;
    return html;
}

function postSearch(keyword) {
    var body = "type=articlename&s=" + java.encodeURI(keyword, "UTF-8");
    var headers = "{\"Content-Type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"User-Agent\":\"Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/150 Mobile Safari/537.36\",\"Referer\":\"" + config.bookSourceUrl + "/\"}";
    var response = java.post(config.bookSourceUrl + "/s.php", body, headers, 20000);
    var html = response == null ? "" : safeString(response.body());
    if (!html) throw config.bookSourceName + "搜索请求失败";
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

function metaContent(doc, selector) {
    var node = doc.selectFirst(selector);
    return node == null ? "" : trimText(node.attr("content"));
}

function firstText(element, selector) {
    if (element == null) return "";
    var node = element.selectFirst(selector);
    return node == null ? "" : trimText(node.text());
}

function bookIdFromUrl(url) {
    var match = safeString(url).match(/\/book\/(\d+)\/?(?:[?#].*)?$/i);
    return match ? match[1] : "";
}

function toCatalogUrl(bookUrl) {
    var id = bookIdFromUrl(bookUrl);
    return id ? config.bookSourceUrl + "/newbook/" + id + "/" : trimText(bookUrl);
}

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword || Number(page) > 1) return [];
    var html = postSearch(keyword);
    var doc = Jsoup.parse(html, config.bookSourceUrl + "/s.php");
    var rows = doc.select("li:has(span.name a[href])");
    var books = [];
    var seen = {};
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("span.name a[href]");
        if (link == null) continue;
        var bookUrl = resolveUrl(config.bookSourceUrl, link.attr("href"));
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl] || !bookIdFromUrl(bookUrl)) continue;
        seen[bookUrl] = true;
        books.push({
            name: name,
            author: firstText(row, "span.zuo a[href]") || firstText(row, "span.zuo"),
            latestChapterTitle: firstText(row, "span.jie a[href]"),
            bookUrl: bookUrl
        });
    }
    return books;
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) throw config.bookSourceName + "书籍地址为空";
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var cover = doc.selectFirst("div.zhutu img");
    var category = metaContent(doc, 'meta[property="og:novel:category"]');
    var status = metaContent(doc, 'meta[property="og:novel:status"]');
    var kind = category;
    if (status) kind = kind ? kind + "," + status : status;
    return {
        name: metaContent(doc, 'meta[property="og:novel:book_name"]') || firstText(doc, "h1") || trimText(book.name),
        author: metaContent(doc, 'meta[property="og:novel:author"]') || firstText(doc, "div.xinxi span.x1 a[href*=author]") || trimText(book.author),
        intro: metaContent(doc, 'meta[property="og:description"]') || firstText(doc, "div.x3"),
        coverUrl: metaContent(doc, 'meta[property="og:image"]') || imageUrl(bookUrl, cover) || trimText(book.coverUrl),
        kind: kind,
        latestChapterTitle: metaContent(doc, 'meta[property="og:novel:latest_chapter_name"]') || firstText(doc, "div.xinxi span.x2 a[href*=chapter]"),
        tocUrl: toCatalogUrl(bookUrl)
    };
}

function collectChapters(doc, pageUrl, bookId, chapters, seen) {
    var links = doc.select("a[href*=\/chapter\/]");
    var pattern = new RegExp("/chapter/" + bookId + "/\\d+\\.html(?:[?#].*)?$", "i");
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i);
        var url = resolveUrl(pageUrl, link.attr("href"));
        var title = trimText(link.text());
        if (!url || !title || !pattern.test(url) || seen[url]) continue;
        seen[url] = true;
        chapters.push({title: title, url: url});
    }
}

function getChapters(book) {
    var bookUrl = trimText(book.bookUrl);
    var bookId = bookIdFromUrl(bookUrl);
    if (!bookId) throw config.bookSourceName + "无法识别书籍编号";
    var tocUrl = trimText(book.tocUrl) || toCatalogUrl(bookUrl);
    var chapters = [];
    var seen = {};
    var firstDoc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    collectChapters(firstDoc, tocUrl, bookId, chapters, seen);
    if (chapters.length < 10 && tocUrl !== bookUrl) {
        var fallbackDoc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
        collectChapters(fallbackDoc, bookUrl, bookId, chapters, seen);
    }
    if (chapters.length === 0) throw config.bookSourceName + "目录为空: " + tocUrl;
    return chapters;
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = trimText(chapter.url);
    if (!chapterUrl) throw config.bookSourceName + "章节地址为空";
    var doc = Jsoup.parse(requestHtml(chapterUrl), chapterUrl);
    var content = doc.selectFirst("#txt");
    if (content == null) throw config.bookSourceName + "正文节点不存在: " + chapterUrl;
    content.select("script,style,iframe,form,p[style*=font-weight],.ads,.ad").remove();
    var text;
    try {
        text = safeString(content.wholeText());
    } catch (error) {
        text = safeString(content.text());
    }
    var lines = text.replace(/\r/g, "").split("\n");
    var pieces = [];
    for (var i = 0; i < lines.length; i++) {
        var line = trimText(lines[i]);
        if (!line) continue;
        if (/\(第\d+\/\d+页\)/.test(line)) continue;
        if (/^正文未完/.test(line) || /^请点击下一页/.test(line)) continue;
        if (/笔趣阁.*更新快/i.test(line) || /xbiquge345\.com|biquge365\.net/i.test(line)) continue;
        pieces.push(line);
    }
    if (pieces.length === 0) throw config.bookSourceName + "正文为空: " + chapterUrl;
    return pieces.join("\n");
}
