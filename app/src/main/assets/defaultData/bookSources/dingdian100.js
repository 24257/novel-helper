var config = {
    bookSourceUrl: "https://www.dingdian100.com",
    bookSourceName: "顶点100",
    bookSourceType: 0,
    bookSourceGroup: "网文小助手内置",
    bookSourceComment: "网文小助手内置公开网页源。搜索有频率限制，不做自动重试。",
    exploreUrl: [
        {title: "\u70ed\u95e8\u699c", url: "https://www.dingdian100.com/"},
        {title: "\u5b8c\u672c\u699c", url: "https://www.dingdian100.com/full/"},
        {title: "\u7384\u5e7b", url: "https://www.dingdian100.com/sort/1_1/"},
        {title: "\u4ed9\u4fa0", url: "https://www.dingdian100.com/sort/2_1/"},
        {title: "\u90fd\u5e02", url: "https://www.dingdian100.com/sort/3_1/"}
    ],
    lastUpdateTime: 1788282000000
};
var Jsoup = org.jsoup.Jsoup;
function s(v){return v==null?"":String(v)}
function t(v){return s(v).replace(/\u00a0|\u3000/g," ").replace(/[\t\r\n]+/g," ").replace(/\s{2,}/g," ").trim()}
function ajax(u){var h=s(java.ajax(s(u),20000));if(!h)throw config.bookSourceName+"请求失败: "+u;return h}
function abs(base,href){var v=t(href);if(!v)return"";if(/^https?:\/\//i.test(v))return v;if(/^\/\//.test(v))return"https:"+v;if(v.charAt(0)==="/")return config.bookSourceUrl+v;var b=s(base).split("#")[0].split("?")[0],p=b.lastIndexOf("/");return p<0?v:b.substring(0,p+1)+v}
function meta(doc,key){var n=doc.selectFirst('meta[property="'+key+'"]');return n==null?"":t(n.attr("content"))}
function text(root,sel){if(root==null)return"";var n=root.selectFirst(sel);return n==null?"":t(n.text())}
function img(base,node){if(node==null)return"";var a=["src","data-src","data-original","data-lazy-src"];for(var i=0;i<a.length;i++){var v=t(node.attr(a[i]));if(v&&v!=="#"&&!/^data:/i.test(v))return abs(base,v)}return""}
function id(url){var m=s(url).match(/\/book\/(\d+)\/?(?:[?#].*)?$/i);return m?m[1]:""}
function search(key,page){var k=t(key);if(!k||Number(page)>1)return[];var body="type=articlename&s="+java.encodeURI(k,"UTF-8");var hd="{\"Content-Type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"User-Agent\":\"Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/150 Mobile Safari/537.36\",\"Referer\":\""+config.bookSourceUrl+"/\"}";var r=java.post(config.bookSourceUrl+"/s.php",body,hd,20000),h=r==null?"":s(r.body());if(!h||/每分钟搜索不得超过/i.test(h))return[];var d=Jsoup.parse(h,config.bookSourceUrl+"/s.php"),rows=d.select("div.lastupdate li, li:has(span.name a[href])"),out=[],seen={};for(var i=0;i<rows.size();i++){var row=rows.get(i),a=row.selectFirst("span.name a[href]");if(a==null)continue;var u=abs(config.bookSourceUrl,a.attr("href")),n=t(a.text());if(!u||!n||!id(u)||seen[u])continue;seen[u]=1;out.push({name:n,author:text(row,"span.zuo a[href]")||text(row,"span.zuo"),kind:text(row,"span.lei"),latestChapterTitle:text(row,"span.jie a[href]"),bookUrl:u})}return out}
function dingBookUrl(base,href){var raw=abs(base,href),m=s(raw).match(/\/(?:book|newbook)\/(\d+)\/?(?:[?#].*)?$/i);return m?config.bookSourceUrl+"/book/"+m[1]+"/":""}
function explore(url,page){
    if(Number(page)>1)return[];
    var pageUrl=t(url);if(!pageUrl)return[];
    var d=Jsoup.parse(ajax(pageUrl),pageUrl),out=[],seen={};
    var cards=d.select("ul.qiangtui li, ul.wanben li");
    for(var i=0;i<cards.size()&&out.length<20;i++){
        var card=cards.get(i),a=card.selectFirst("h3 a[href], a[href*=/book/], a[href*=/newbook/]");if(a==null)continue;
        var u=dingBookUrl(pageUrl,a.attr("href")),coverUrl=img(pageUrl,card.selectFirst("img")),name=text(card,"h3.p2 a[href], h3 a[href]")||t(a.text());
        if(!u||!name||!coverUrl||/zanwu\.jpg/i.test(coverUrl)||seen[u])continue;
        seen[u]=1;
        out.push({name:name,intro:text(card,"p.p3"),coverUrl:coverUrl,bookUrl:u,tocUrl:config.bookSourceUrl+"/newbook/"+id(u)+"/"});
    }
    if(out.length)return out;
    var rows=d.select("li:has(span.name a[href])"),candidates=[];
    for(var j=0;j<rows.size()&&candidates.length<10;j++){
        var row=rows.get(j),link=row.selectFirst("span.name a[href]");if(link==null)continue;
        var bookUrl=dingBookUrl(pageUrl,link.attr("href")),bookName=t(link.text()).replace(/^[《\s]+|[》\s]+$/g,"");
        if(!bookUrl||!bookName||seen[bookUrl])continue;
        seen[bookUrl]=1;
        candidates.push({name:bookName,author:text(row,"span.zuo"),kind:text(row,"span.lei"),latestChapterTitle:text(row,"span.jie a[href]"),bookUrl:bookUrl});
    }
    if(!candidates.length)return[];
    var urls=[];for(var k=0;k<candidates.length;k++)urls.push(candidates[k].bookUrl);
    var responses=java.ajaxAll(urls,true);
    for(var r=0;r<responses.length;r++){
        try{
            var candidate=candidates[r],html=responses[r]==null?"":s(responses[r].body());if(!html)continue;
            var detail=Jsoup.parse(html,candidate.bookUrl),bid=id(candidate.bookUrl),coverUrl=meta(detail,"og:image")||img(candidate.bookUrl,detail.selectFirst("div.zhutu img, img[src*=files/article/image/]"));
            if(!coverUrl||/zanwu\.jpg/i.test(coverUrl))continue;
            out.push({name:meta(detail,"og:novel:book_name")||meta(detail,"og:title")||candidate.name,author:meta(detail,"og:novel:author")||candidate.author,intro:meta(detail,"og:description")||text(detail,"div.x3, div.intro"),coverUrl:coverUrl,kind:meta(detail,"og:novel:category")||candidate.kind,latestChapterTitle:meta(detail,"og:novel:latest_chapter_name")||candidate.latestChapterTitle,bookUrl:candidate.bookUrl,tocUrl:bid?config.bookSourceUrl+"/newbook/"+bid+"/":candidate.bookUrl});
        }catch(e){}
    }
    return out;
}
function getBookInfo(book){var u=t(book.bookUrl);if(!u)throw config.bookSourceName+"书籍地址为空";var d=Jsoup.parse(ajax(u),u),bid=id(u),cat=meta(d,"og:novel:category"),st=meta(d,"og:novel:status");return{name:meta(d,"og:novel:book_name")||meta(d,"og:title")||text(d,"h1")||t(book.name),author:meta(d,"og:novel:author")||text(d,"div.xinxi span.x1 a[href]")||t(book.author),intro:meta(d,"og:description")||text(d,"div.x3, div.intro"),coverUrl:meta(d,"og:image")||img(u,d.selectFirst("div.zhutu img, img[src*=files/article/image/]"))||t(book.coverUrl),kind:cat+(st?(cat?",":"")+st:""),latestChapterTitle:meta(d,"og:novel:latest_chapter_name")||text(d,"div.xinxi span.x2 a[href]"),tocUrl:bid?config.bookSourceUrl+"/newbook/"+bid+"/":u}}
function getChapters(book){var bid=id(book.bookUrl),u=t(book.tocUrl)||(bid?config.bookSourceUrl+"/newbook/"+bid+"/":t(book.bookUrl));if(!bid||!u)throw config.bookSourceName+"目录地址无效";var d=Jsoup.parse(ajax(u),u),links=d.select('div.border ul.info a[href*="/chapter/'+bid+'/"], ul.info a[href*="/chapter/'+bid+'/"]'),out=[],seen={},re=new RegExp("/chapter/"+bid+"/[^/?#]+\\.html(?:[?#].*)?$","i");for(var i=0;i<links.size();i++){var a=links.get(i),cu=abs(u,a.attr("href")),name=t(a.attr("title"))||t(a.text());if(!name||!re.test(cu)||seen[cu])continue;seen[cu]=1;out.push({title:name,url:cu})}if(!out.length)throw config.bookSourceName+"目录为空: "+u;return out}
function getContent(chapter,book,nextChapterUrl){var u=t(chapter.url);if(!u)throw config.bookSourceName+"章节地址为空";var d=Jsoup.parse(ajax(u),u),n=d.selectFirst("#txt");if(n==null)n=d.selectFirst("#booktxt");if(n==null)n=d.selectFirst("div.content, div.readcontent");if(n==null)throw config.bookSourceName+"正文节点不存在: "+u;n.select("script,style,iframe,form,.ad,.ads").remove();var v;try{v=s(n.wholeText())}catch(e){v=s(n.text())}v=v.replace(/\r/g,"").replace(/\n{3,}/g,"\n\n").replace(/本章未完[^\n]*/g,"").replace(/请点击下一页[^\n]*/g,"").trim();if(!v)throw config.bookSourceName+"正文为空: "+u;return v}
