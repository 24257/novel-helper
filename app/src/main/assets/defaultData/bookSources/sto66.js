var config = {
    bookSourceUrl: "https://www.sto66.com",
    bookSourceName: "思兔阅读",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。无需登录，按 sto66.com 当前公开页面结构适配。",
    exploreUrl: [],
    lastUpdateTime: 1788152400000
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
    if (!html) {
        throw "思兔阅读请求失败: " + url;
    }
    return html;
}

function resolveUrl(base, href) {
    var value = trimText(href);
    if (!value) {
        return "";
    }
    if (/^https?:\/\//i.test(value)) {
        return value;
    }
    if (/^\/\//.test(value)) {
        return "https:" + value;
    }
    if (value.charAt(0) === "/") {
        return config.bookSourceUrl + value;
    }
    var cleanBase = safeString(base).split("#")[0].split("?")[0];
    var slash = cleanBase.lastIndexOf("/");
    if (slash < 0) {
        return value;
    }
    return cleanBase.substring(0, slash + 1) + value;
}

function imageUrl(base, element) {
    if (element == null) return "";
    var attrs = ["src", "data-src", "data-original", "data-lazy-src", "data-url"];
    for (var i = 0; i < attrs.length; i++) {
        var value = trimText(element.attr(attrs[i]));
        if (value && value !== "#" && !/^data:/i.test(value)) {
            return resolveUrl(base, value);
        }
    }
    return "";
}

function bookIdFromUrl(url) {
    var match = safeString(url).match(/\/book\/([^\/?#]+)\.html/i);
    return match ? match[1] : "";
}

function toCatalogUrl(bookUrl) {
    var id = bookIdFromUrl(bookUrl);
    return id ? config.bookSourceUrl + "/chapter/" + id + ".html" : "";
}

function isChapterUrl(url) {
    return /\/chapter\/[^\/?#]+\/[^\/?#]+\.html(?:[?#].*)?$/i.test(safeString(url));
}

function firstText(element, selector) {
    if (element == null) {
        return "";
    }
    var target = element.selectFirst(selector);
    return target == null ? "" : trimText(target.text());
}

function parseAuthor(text) {
    return trimText(text).replace(/^作者\s*[:：]?\s*/, "");
}

function search(key, page) {
    var keyword = trimText(key);
    if (!keyword) {
        return [];
    }

    var safePage = Number(page) > 0 ? Number(page) : 1;
    var encoded = java.encodeURI(keyword, "UTF-8");
    var url = safePage === 1
        ? config.bookSourceUrl + "/search/" + encoded + ".html"
        : config.bookSourceUrl + "/search/" + encoded + "/" + safePage + ".html";
    var doc = Jsoup.parse(requestHtml(url), url);
    var rows = doc.select("div.bookbox");
    var books = [];
    var seen = {};

    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("h2.bookname a[href]");
        if (link == null) {
            continue;
        }
        var bookUrl = resolveUrl(url, link.attr("href"));
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl]) {
            continue;
        }
        seen[bookUrl] = true;

        var author = "";
        var authorNodes = row.select("div.author");
        if (!authorNodes.isEmpty()) {
            author = parseAuthor(authorNodes.get(0).text());
        }
        var latest = firstText(row, "div.cat a[href]");
        var intro = firstText(row, "div.update").replace(/^简介\s*[:：]?\s*/, "");
        books.push({
            name: name,
            author: author,
            intro: intro,
            latestChapterTitle: latest,
            bookUrl: bookUrl
        });
    }

    return books;
}

function getBookInfo(book) {
    var bookUrl = trimText(book.bookUrl);
    if (!bookUrl) {
        throw "思兔阅读书籍地址为空";
    }

    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var info = doc.selectFirst("div.bookinfo");
    var cover = doc.selectFirst("div.bookcover img.thumbnail");
    var tags = info == null ? null : info.select("p.booktag a");
    var author = "";
    var kind = "";
    if (tags != null && !tags.isEmpty()) {
        author = trimText(tags.get(0).text());
        if (tags.size() > 1) {
            kind = trimText(tags.get(1).text());
        }
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
        coverUrl: imageUrl(bookUrl, cover) || trimText(book.coverUrl),
        kind: kind,
        latestChapterTitle: firstText(info, "a.bookchapter[href]"),
        tocUrl: toCatalogUrl(bookUrl)
    };
}

function appendChaptersFromDoc(doc, pageUrl, chapters, seen) {
    var links = doc.select("#allchapter dd a[href]");
    if (links.isEmpty()) {
        links = doc.select("div.chapterlist dd a[href]");
    }
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i);
        var url = resolveUrl(pageUrl, link.attr("href"));
        var title = trimText(link.text());
        if (!isChapterUrl(url) || !title || seen[url]) {
            continue;
        }
        seen[url] = true;
        chapters.push({
            title: title,
            url: url
        });
    }
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || toCatalogUrl(book.bookUrl);
    if (!tocUrl) {
        throw "思兔阅读目录地址为空";
    }

    var firstDoc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    var chapters = [];
    var seen = {};
    appendChaptersFromDoc(firstDoc, tocUrl, chapters, seen);

    var pageOptions = firstDoc.select("#linkIndex option[value]");
    for (var i = 0; i < pageOptions.size(); i++) {
        var option = pageOptions.get(i);
        if (option.hasAttr("selected")) {
            continue;
        }
        var pageUrl = resolveUrl(tocUrl, option.attr("value"));
        if (!pageUrl) {
            continue;
        }
        var pageDoc = Jsoup.parse(requestHtml(pageUrl), pageUrl);
        appendChaptersFromDoc(pageDoc, pageUrl, chapters, seen);
    }

    return chapters;
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = trimText(chapter.url);
    if (!chapterUrl) {
        throw "思兔阅读章节地址为空";
    }

    var doc = Jsoup.parse(requestHtml(chapterUrl), chapterUrl);
    var content = doc.selectFirst("#content.readcontent");
    if (content == null) {
        content = doc.selectFirst("#content");
    }
    if (content == null) {
        throw "思兔阅读正文节点不存在: " + chapterUrl;
    }

    content.select("script,style,iframe,form,.ads,.adv").remove();
    var paragraphs = content.select("p");
    var pieces = [];
    for (var i = 0; i < paragraphs.size(); i++) {
        var text = trimText(paragraphs.get(i).text());
        if (text) {
            pieces.push(text);
        }
    }
    if (pieces.length > 0) {
        return pieces.join("\n");
    }

    var text = "";
    try {
        text = safeString(content.wholeText());
    } catch (error) {
        text = safeString(content.text());
    }
    return text
        .replace(/\r/g, "")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
}
