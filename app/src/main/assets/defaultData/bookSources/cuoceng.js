var config = {
    bookSourceUrl: "https://m.cuoceng.com",
    bookSourceName: "错层小说",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。按 m.cuoceng.com 当前移动页面结构适配。",
    exploreUrl: [
        {title: "\u70ed\u95e8\u699c", url: "https://m.cuoceng.com/book/ranking.html"},
        {title: "\u5b8c\u672c\u699c", url: "https://m.cuoceng.com/book/finish.html"},
        {title: "\u7384\u5e7b", url: "https://m.cuoceng.com/book/category/catalog.html"}
    ],
    lastUpdateTime: 1788282000000
};

var Jsoup = org.jsoup.Jsoup;
function safeString(value) { return value == null ? "" : String(value); }
function trimText(value) {
    return safeString(value).replace(/\u00a0/g, " ").replace(/\u3000/g, " ")
        .replace(/[\t\r\n]+/g, " ").replace(/\s{2,}/g, " ").trim();
}
function requestHtml(url) {
    var html = safeString(java.ajax(safeString(url), 20000));
    if (!html) throw "错层小说请求失败: " + url;
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
function bookIdFromUrl(url) {
    var m = safeString(url).match(/\/book\/([^\/?#]+)\.html/i);
    return m ? m[1] : "";
}
function toCatalogUrl(bookUrl) {
    var id = bookIdFromUrl(bookUrl);
    return id ? config.bookSourceUrl + "/book/chapter/" + id + ".html" : "";
}
function isChapterUrl(url) {
    return /\/book\/[^\/?#]+\/[0-9a-f-]+\.html(?:[?#].*)?$/i.test(safeString(url));
}

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword) return [];
    var safePage = Number(page) > 0 ? Number(page) : 1;
    var encoded = java.encodeURI(keyword, "UTF-8");
    var url = safePage === 1
        ? config.bookSourceUrl + "/book/so/" + encoded + ".html"
        : config.bookSourceUrl + "/book/so/" + encoded + "/" + safePage + ".html";
    var doc = Jsoup.parse(requestHtml(url), url);
    var rows = doc.select("div.bookbox");
    var books = [];
    var seen = {};
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h2.bookname a[href]");
        if (link == null) continue;
        var bookUrl = resolveUrl(url, link.attr("href"));
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl]) continue;
        seen[bookUrl] = true;
        var authors = row.select("div.author");
        books.push({
            name: name,
            author: authors.isEmpty() ? "" : parseAuthor(authors.get(0).text()),
            intro: firstText(row, "div.update").replace(/^简介\s*[:：]?\s*/, ""),
            latestChapterTitle: firstText(row, "div.cat a[href]"),
            bookUrl: bookUrl
        });
    }
    return books;
}

function explore(url, page) {
    if (Number(page) > 1) return [];
    var pageUrl = trimText(url);
    if (!pageUrl) return [];
    var doc = Jsoup.parse(requestHtml(pageUrl), pageUrl);
    var links = doc.select("a[href]");
    var candidates = [];
    var seen = {};
    for (var i = 0; i < links.size() && candidates.length < 12; i++) {
        var link = links.get(i);
        var bookUrl = resolveUrl(pageUrl, link.attr("href"));
        var name = trimText(link.text());
        if (!/^https?:\/\/m\.cuoceng\.com\/book\/[^\/?#]+\.html(?:[?#].*)?$/i.test(bookUrl) || !name || seen[bookUrl]) continue;
        seen[bookUrl] = true;
        candidates.push({name: name, bookUrl: bookUrl});
    }
    if (!candidates.length) return [];
    var urls = [];
    for (var j = 0; j < candidates.length; j++) urls.push(candidates[j].bookUrl);
    var responses = java.ajaxAll(urls, true);
    var books = [];
    for (var r = 0; r < responses.length; r++) {
        try {
            var candidate = candidates[r];
            var html = responses[r] == null ? "" : safeString(responses[r].body());
            if (!html) continue;
            var detail = Jsoup.parse(html, candidate.bookUrl);
            var info = detail.selectFirst("div.bookinfo");
            var cover = detail.selectFirst("div.bookcover img.thumbnail, div.bookcover img, img.thumbnail, img[src*=bookCover], img[src*=cover]");
            var coverUrl = imageUrl(candidate.bookUrl, cover);
            if (!coverUrl) continue;
            var tags = info == null ? null : info.select("p.booktag a");
            var author = tags != null && !tags.isEmpty() ? trimText(tags.get(0).text()) : "";
            var kind = tags != null && tags.size() > 1 ? trimText(tags.get(1).text()) : "";
            books.push({
                name: firstText(info, "h1.booktitle") || candidate.name,
                author: author,
                intro: firstText(info, "p.bookintro"),
                coverUrl: coverUrl,
                kind: kind,
                latestChapterTitle: firstText(info, "a.bookchapter[href]"),
                bookUrl: candidate.bookUrl,
                tocUrl: toCatalogUrl(candidate.bookUrl)
            });
        } catch (e) {}
    }
    return books;
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) throw "错层小说书籍地址为空";
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var info = doc.selectFirst("div.bookinfo");
    var cover = doc.selectFirst("div.bookcover img.thumbnail, div.bookcover img, img.thumbnail, img[src*=bookCover], img[src*=cover]");
    var ogImage = doc.selectFirst('meta[property="og:image"]');
    var tags = info == null ? null : info.select("p.booktag a");
    var author = "";
    var kind = "";
    if (tags != null && !tags.isEmpty()) {
        author = trimText(tags.get(0).text());
        if (tags.size() > 1) kind = trimText(tags.get(1).text());
    }
    if (info != null) {
        var spans = info.select("p.booktag span");
        for (var i = 0; i < spans.size(); i++) {
            var value = trimText(spans.get(i).text());
            if (value === "全本" || value === "连载" || value === "完结") {
                kind = kind ? kind + "," + value : value;
                break;
            }
        }
    }
    return {
        name: firstText(info, "h1.booktitle") || trimText(book.name),
        author: author || trimText(book.author),
        intro: firstText(info, "p.bookintro"),
        coverUrl: imageUrl(bookUrl, cover) || (ogImage == null ? "" : trimText(ogImage.attr("content"))) || trimText(book.coverUrl),
        kind: kind,
        latestChapterTitle: firstText(info, "a.bookchapter[href]"),
        tocUrl: toCatalogUrl(bookUrl)
    };
}

function appendChapters(doc, pageUrl, chapters, seen) {
    var links = doc.select("#allchapter dd a[href]");
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i);
        var url = resolveUrl(pageUrl, link.attr("href"));
        var title = trimText(link.text());
        if (!isChapterUrl(url) || !title || seen[url]) continue;
        seen[url] = true;
        chapters.push({title: title, url: url});
    }
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || toCatalogUrl(book.bookUrl);
    if (!tocUrl) throw "错层小说目录地址为空";
    var firstDoc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    var chapters = [];
    var seen = {};
    appendChapters(firstDoc, tocUrl, chapters, seen);
    var options = firstDoc.select("#linkIndex option[value]");
    for (var i = 0; i < options.size(); i++) {
        var option = options.get(i);
        if (option.hasAttr("selected")) continue;
        var pageUrl = resolveUrl(tocUrl, option.attr("value"));
        if (!pageUrl) continue;
        appendChapters(Jsoup.parse(requestHtml(pageUrl), pageUrl), pageUrl, chapters, seen);
    }
    return chapters;
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = trimText(chapter.url);
    if (!chapterUrl) throw "错层小说章节地址为空";
    var doc = Jsoup.parse(requestHtml(chapterUrl), chapterUrl);
    var content = doc.selectFirst("#content.readcontent");
    if (content == null) content = doc.selectFirst("#content");
    if (content == null) throw "错层小说正文节点不存在: " + chapterUrl;
    content.select("script,style,iframe,form,.ads,.adv").remove();
    var ps = content.select("p");
    var pieces = [];
    for (var i = 0; i < ps.size(); i++) {
        var text = trimText(ps.get(i).text());
        if (text) pieces.push(text);
    }
    return pieces.length > 0 ? pieces.join("\n") : trimText(content.text());
}
