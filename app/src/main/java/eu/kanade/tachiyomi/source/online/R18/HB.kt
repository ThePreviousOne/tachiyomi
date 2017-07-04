package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Protocol
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber

class HB : Base() {

    override val id: Long = 17L

    override val name: String = "HentaiBeast"

    override val baseUrl = "http://hentaibeast.com/"

    override fun pageUrl() = "$baseUrl/"

    override fun imageElement() = "figure#theImage > img"

    override fun popularMangaSelector() = "article#content > ul.thumbnails > li.span3"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("figure > figcaption > h3 > a").let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.text()
        }
        return manga
    }

    override fun popularMangaNextPageSelector() = "div.navigationBar > a.next:contains(Next)"

    override fun mangaDetailsParse(response: Response): SManga {
        val manga = SManga.create()
        manga.thumbnail_url = pageUrl() + response.asJsoup().select("ul#thumbnails > li:eq(0) > a > img").attr("src")
        manga.author = ".."
        manga.artist = ".."
        manga.status = SManga.UNKNOWN
        return manga
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        val chapter = SChapter.create().apply {
            this.setUrlWithoutDomain(response.asJsoup().select("ul#thumbnails > li > a").attr("href"))
            this.name = "No Title"
            chapters.add(this)
        }
        pageListParse(client.newCall(pageListRequest(chapter)).execute().asJsoup())
        return chapters
    }

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        Timber.i("2")
        var pageUrlNum = document.location().substringAfter("?/").substringBefore("/category").toInt()

        for (i in 1..document.select("article#content ul.headerActions > li").text().substringAfter('/').toInt()) {
            pages.add(Page(i, document.location().substring(0, 36)
                    + "$pageUrlNum/cat" + document.location().substringAfter("/cat")))
            pageUrlNum++
        }
        Timber.i("3")
        return pages
    }

    override fun imageUrlParse(document: Document): String = baseUrl + document.select(imageElement()).attr("src")

    override fun chapterListSelector()  = ""

}