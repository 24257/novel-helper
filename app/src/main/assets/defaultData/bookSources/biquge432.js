var config = {
    bookSourceUrl: "https://www.biquge432.com",
    bookSourceName: "笔趣阁432",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。按当前公开页面结构解析；站点搜索有频率限制，不做自动重试。",
    exploreUrl: [],
    lastUpdateTime: 1788175200000
};

var Jsoup = org.jsoup.Jsoup;

function safeString(v) { return v == null ? "" : String(v); }
function trimText(v) {
    return safeString(v).replace(/\u00a0/g, " ").replace(/\u3000/g, " ")
        .replace(/[\t\r\n]+/g, " ").replace(/\s{2,}/g, " ").trim();
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
    if (/每分钟搜索不得超过/i.test(html)) return "";
    return html;
}
function resolveUrl(base, href) {
    var value = trimText(href);
    if (!value) return "";
    if (/^https?:\/\//i.test(value)) return value;
    if (/^\/\//.test(value)) return "https:" + value;
    if (value.charAt(0) === "/") return config.bookSourceUrl + value;
    var clean = safeString(base).split("#")[0].split("?")[0];
    var slash = clean.lastIndexOf("/");
    return slash < 0 ? value : clean.substring(0, slash + 1) + value;
}
function imageUrl(base, node) {
    if (node == null) return "";
    var attrs = ["src", "data-src", "data-original", "data-lazy-src", "data-url"];
    for (var i = 0; i < attrs.length; i++) {
        var v = trimText(node.attr(attrs[i]));
        if (v && v !== "#" && !/^data:/i.test(v)) return resolveUrl(base, v);
    }
    return "";
}
function meta(doc, key) {
    var n = doc.selectFirst('meta[property="' + key + '"]');
    return n == null ? "" : trimText(n.attr("content"));
}
function firstText(root, selector) {
    if (root == null) return "";
    var n = root.selectFirst(selector);
    return n == null ? "" : trimText(n.text());
}
function bookId(url) {
    var m = safeString(url).match(/\/(?:xiaoshuo|book)\/(\d+)\/?(?:[?#].*)?$/i);
    return m ? m[1] : "";
}
function search(key, page) {
    var keyword = trimText(key);
    if (!keyword || Number(page) > 1) return [];
    var html = postSearch(keyword);
    if (!html) return [];
    var doc = Jsoup.parse(html, config.bookSourceUrl + "/s.php");
    var rows = doc.select("div.lastupdate li, div.update li, li:has(span.name a[href])");
    var result = [], seen = {};
    for (var i = 0; i < rows.size(); i++) {
        var row = rows.get(i);
        var link = row.selectFirst("span.name a[href]");
        if (link == null) continue;
        var url = resolveUrl(config.bookSourceUrl, link.attr("href"));
        var name = trimText(link.text());
        if (!url || !name || !bookId(url) || seen[url]) continue;
        seen[url] = true;
        result.push({
            name: name,
            author: firstText(row, "span.zuo a[href]") || firstText(row, "span.zuo"),
            kind: firstText(row, "span.lei"),
            latestChapterTitle: firstText(row, "span.jie a[href]"),
            bookUrl: url
        });
    }
    return result;
}
function getBookInfo(book) {
    var url = trimText(book.bookUrl);
    if (!url) throw config.bookSourceName + "书籍地址为空";
    var doc = Jsoup.parse(requestHtml(url), url);
    var cover = meta(doc, "og:image") || imageUrl(url, doc.selectFirst("div.zhutu img, img[src*=files/article/image/]"));
    var author = meta(doc, "og:novel:author") || firstText(doc, "div.xinxi span.x1 a[href]") || trimText(book.author);
    var category = meta(doc, "og:novel:category");
    var status = meta(doc, "og:novel:status");
    return {
        name: meta(doc, "og:novel:book_name") || meta(doc, "og:title") || firstText(doc, "h1") || trimText(book.name),
        author: author.replace(/^作者\s*[:：]?\s*/, ""),
        intro: meta(doc, "og:description") || firstText(doc, "div.hang_3, div.x3, div.intro"),
        coverUrl: cover || trimText(book.coverUrl),
        kind: category + (status ? (category ? "," : "") + status : ""),
        latestChapterTitle: meta(doc, "og:novel:latest_chapter_name") || firstText(doc, "span.x2 a[href]"),
        tocUrl: url
    };
}
function getChapters(book) {
    var url = trimText(book.tocUrl) || trimText(book.bookUrl);
    var id = bookId(book.bookUrl || url);
    if (!url || !id) throw config.bookSourceName + "无法识别目录地址";
    var doc = Jsoup.parse(requestHtml(url), url);
    var links = doc.select('a[href*="/zhangjie/' + id + '/"]');
    var result = [], seen = {};
    var pattern = new RegExp("/zhangjie/" + id + "/[^/?#]+\\.html(?:[?#].*)?$", "i");
    for (var i = 0; i < links.size(); i++) {
        var link = links.get(i), chapterUrl = resolveUrl(url, link.attr("href"));
        var title = trimText(link.attr("title")) || trimText(link.text());
        if (!title || !pattern.test(chapterUrl) || seen[chapterUrl]) continue;
        seen[chapterUrl] = true;
        result.push({title: title, url: chapterUrl});
    }
    if (result.length === 0) throw config.bookSourceName + "目录为空: " + url;
    return result;
}
function cleanContent(node) {
    if (node == null) return "";
    node.select("script,style,iframe,form,.ads,.ad").remove();
    var text;
    try { text = safeString(node.wholeText()); } catch (e) { text = safeString(node.text()); }
    return text.replace(/\r/g, "").replace(/\n{3,}/g, "\n\n")
        .replace(/本章未完[^\n]*/g, "").replace(/请点击下一页[^\n]*/g, "").trim();
}
function getContent(chapter, book, nextChapterUrl) {
    var url = trimText(chapter.url);
    if (!url) throw config.bookSourceName + "章节地址为空";
    var doc = Jsoup.parse(requestHtml(url), url);
    var node = doc.selectFirst("#txt");
    if (node == null) node = doc.selectFirst("#booktxt");
    if (node == null) node = doc.selectFirst("div.content, div.readcontent");
    var text = cleanContent(node);
    if (!text) throw config.bookSourceName + "正文为空: " + url;
    return text;
}
