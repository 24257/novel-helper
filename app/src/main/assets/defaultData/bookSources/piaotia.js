var config = {
    bookSourceUrl: "https://www.piaotia.com",
    bookSourceName: "飘天文学",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置书源。站点为 GBK 编码，脚本通过 Legado 网络层读取原始字节后显式解码。",
    exploreUrl: [],
    lastUpdateTime: 1788094800000
};

var Jsoup = org.jsoup.Jsoup;
var REQUEST_OPTIONS = ',{"type":"binary","retry":1,"timeout":20000}';

function safeString(value) {
    return value == null ? "" : String(value);
}

function trimText(value) {
    return safeString(value)
        .replace(/\u00a0/g, " ")
        .replace(/[\t\r\n]+/g, " ")
        .replace(/\s{2,}/g, " ")
        .trim();
}

function requestHtml(url) {
    var hex = java.ajax(safeString(url) + REQUEST_OPTIONS);
    var raw = safeString(hex).trim();
    if (!raw || raw.length % 2 !== 0 || !/^[0-9a-f]+$/i.test(raw)) {
        throw "飘天请求失败: " + url;
    }
    var bytes = java.hexDecodeToByteArray(raw);
    if (bytes == null) {
        throw "飘天响应解码失败: " + url;
    }
    return java.bytesToStr(bytes, "GBK");
}

function resolveUrl(base, href) {
    var value = safeString(href).trim();
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

function isBookUrl(url) {
    return /\/bookinfo\/\d+\/\d+\.html(?:\?.*)?$/i.test(safeString(url));
}

function isCatalogUrl(url) {
    return /\/html\/\d+\/\d+\/(?:index\.html)?(?:\?.*)?$/i.test(safeString(url));
}

function isChapterUrl(url) {
    return /\/html\/\d+\/\d+\/\d+\.html(?:\?.*)?$/i.test(safeString(url));
}

function toBookUrl(url) {
    var value = safeString(url);
    if (isBookUrl(value)) {
        return value;
    }
    var match = value.match(/\/html\/(\d+)\/(\d+)(?:\/|$)/i);
    if (!match) {
        return "";
    }
    return config.bookSourceUrl + "/bookinfo/" + match[1] + "/" + match[2] + ".html";
}

function toCatalogUrl(bookUrl) {
    var match = safeString(bookUrl).match(/\/bookinfo\/(\d+)\/(\d+)\.html(?:\?.*)?$/i);
    if (!match) {
        return "";
    }
    return config.bookSourceUrl + "/html/" + match[1] + "/" + match[2] + "/index.html";
}

function extractLabeledValue(doc, labels) {
    var expected = labels instanceof Array ? labels : [labels];
    var cells = doc.select("td");
    for (var i = 0; i < cells.size(); i++) {
        var compact = trimText(cells.get(i).text()).replace(/\s+/g, "");
        if (!compact) {
            continue;
        }
        for (var j = 0; j < expected.length; j++) {
            var label = safeString(expected[j]).replace(/\s+/g, "");
            if (compact.indexOf(label) !== 0) {
                continue;
            }
            var colon = compact.indexOf("：");
            if (colon < 0) {
                colon = compact.indexOf(":");
            }
            if (colon >= 0 && colon + 1 < compact.length) {
                return trimText(compact.substring(colon + 1));
            }
        }
    }
    return "";
}

function findHotTextLabel(doc, keyword) {
    var labels = doc.select("span.hottext");
    for (var i = 0; i < labels.size(); i++) {
        if (trimText(labels.get(i).text()).indexOf(keyword) >= 0) {
            return labels.get(i);
        }
    }
    return null;
}

function extractLatestChapter(doc) {
    var label = findHotTextLabel(doc, "最新章节");
    if (label == null) {
        return "";
    }
    var node = label.nextSibling();
    while (node != null) {
        if (safeString(node.nodeName()).toLowerCase() === "a") {
            return trimText(node.text());
        }
        node = node.nextSibling();
    }
    return "";
}

function extractIntro(doc) {
    var label = findHotTextLabel(doc, "内容简介");
    if (label == null || label.parent() == null) {
        return "";
    }
    var parent = label.parent();
    var html = safeString(parent.html());
    var marker = safeString(label.outerHtml());
    var pos = html.indexOf(marker);
    if (pos >= 0) {
        html = html.substring(pos + marker.length);
    }
    html = html.replace(/^\s*<br\s*\/?\s*>/i, "");
    var fragment = Jsoup.parseBodyFragment(html);
    fragment.select("script,style,table,form,iframe").remove();
    var intro = trimText(fragment.body().text());
    intro = intro.replace(/展开全部.*$/g, "").trim();
    return intro;
}

function search(key, page) {
    var safePage = Number(page) > 0 ? Number(page) : 1;
    var encoded = java.encodeURI(trimText(key), "GBK");
    var url = config.bookSourceUrl
        + "/modules/article/search.php?searchtype=articlename&searchkey="
        + encoded + "&page=" + safePage;
    var doc = Jsoup.parse(requestHtml(url), url);
    var rows = doc.select("table.grid tr:has(td)");
    var books = [];
    var seen = {};

    for (var i = 0; i < rows.size(); i++) {
        var cols = rows.get(i).select("td");
        if (cols.size() < 1) {
            continue;
        }
        var link = cols.get(0).selectFirst("a[href]");
        if (link == null) {
            continue;
        }
        var resolved = resolveUrl(url, link.attr("href"));
        var bookUrl = toBookUrl(resolved);
        var name = trimText(link.text());
        if (!bookUrl || !name || seen[bookUrl]) {
            continue;
        }
        seen[bookUrl] = true;
        books.push({
            name: name,
            author: cols.size() > 2 ? trimText(cols.get(2).text()) : "",
            bookUrl: bookUrl
        });
    }

    if (books.length === 0) {
        var links = doc.select("a[href*=/html/]");
        for (var k = 0; k < links.size(); k++) {
            var candidate = toBookUrl(resolveUrl(url, links.get(k).attr("href")));
            if (!candidate) {
                continue;
            }
            var titleElement = doc.selectFirst("h1");
            var title = titleElement == null ? trimText(doc.title()) : trimText(titleElement.text());
            if (title && title.indexOf(trimText(key)) >= 0) {
                books.push({
                    name: title,
                    author: extractLabeledValue(doc, ["作者", "文章作者"]),
                    bookUrl: candidate,
                    coverUrl: resolveUrl(candidate, doc.select("img[src*=files/article/image/]").attr("src")),
                    intro: extractIntro(doc)
                });
            }
            break;
        }
    }

    return books;
}

function getBookInfo(book) {
    var bookUrl = safeString(book.bookUrl);
    var doc = Jsoup.parse(requestHtml(bookUrl), bookUrl);
    var titleElement = doc.selectFirst("h1");
    var cover = doc.select("img[src*=files/article/image/]").attr("src");
    var category = extractLabeledValue(doc, ["类别", "文章类别"]);
    var status = extractLabeledValue(doc, ["文章状态", "状态"]);
    var kind = category;
    if (status) {
        kind = kind ? kind + "," + status : status;
    }

    var tocUrl = toCatalogUrl(bookUrl);
    if (!tocUrl) {
        var links = doc.select("a[href*=/html/]");
        for (var i = 0; i < links.size(); i++) {
            var resolved = resolveUrl(bookUrl, links.get(i).attr("href"));
            var match = resolved.match(/\/html\/(\d+)\/(\d+)\//i);
            if (match) {
                tocUrl = config.bookSourceUrl + "/html/" + match[1] + "/" + match[2] + "/index.html";
                break;
            }
        }
    }

    return {
        name: titleElement == null ? trimText(doc.title()) : trimText(titleElement.text()),
        author: extractLabeledValue(doc, ["作者", "文章作者"]),
        intro: extractIntro(doc),
        coverUrl: resolveUrl(bookUrl, cover),
        kind: kind,
        latestChapterTitle: extractLatestChapter(doc),
        tocUrl: tocUrl || bookUrl
    };
}

function getChapters(book) {
    var tocUrl = trimText(book.tocUrl) || toCatalogUrl(book.bookUrl);
    if (!tocUrl) {
        throw "无法生成飘天目录地址";
    }
    var doc = Jsoup.parse(requestHtml(tocUrl), tocUrl);
    var links = doc.select("table.grid li a[href]");
    if (links.isEmpty()) {
        links = doc.select("li a[href]");
    }
    var chapters = [];
    var seen = {};
    for (var i = 0; i < links.size(); i++) {
        var url = resolveUrl(tocUrl, links.get(i).attr("href"));
        var title = trimText(links.get(i).text());
        if (!isChapterUrl(url) || !title || seen[url]) {
            continue;
        }
        seen[url] = true;
        chapters.push({ title: title, url: url });
    }
    return chapters;
}

function getContent(chapter, book, nextChapterUrl) {
    var chapterUrl = safeString(chapter.url);
    var doc = Jsoup.parse(requestHtml(chapterUrl), chapterUrl);
    var top = doc.selectFirst("div.toplink");
    var pieces = [];

    if (top != null) {
        var node = top.nextSibling();
        while (node != null) {
            var nodeName = safeString(node.nodeName()).toLowerCase();
            if (nodeName === "div" && safeString(node.attr("class")).indexOf("bottomlink") >= 0) {
                break;
            }
            if (nodeName !== "table"
                && nodeName !== "script"
                && nodeName !== "style"
                && nodeName !== "center"
                && nodeName !== "form"
                && nodeName !== "iframe") {
                pieces.push(safeString(node.outerHtml()));
            }
            node = node.nextSibling();
        }
    }

    var html = pieces.join("\n");
    if (!html) {
        html = safeString(doc.body().html());
    }
    var fragment = Jsoup.parseBodyFragment(html);
    fragment.select("script,style,table,center,form,iframe,.toplink,.bottomlink,#Commenddiv,#feit2").remove();

    var text = "";
    try {
        text = safeString(fragment.body().wholeText());
    } catch (error) {
        text = safeString(fragment.body().text());
    }

    return text
        .replace(/\r/g, "")
        .replace(/[ \t]+\n/g, "\n")
        .replace(/\n[ \t]+/g, "\n")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
}
