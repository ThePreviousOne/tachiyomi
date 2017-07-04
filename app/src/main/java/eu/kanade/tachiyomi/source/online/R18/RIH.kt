package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class RIH : Base() {

    override val id: Long = 20L

    override val name: String = "ReadIncestHentai"

    override val baseUrl = "http://ReadIncestHentai.com"

    override fun pageUrl() = "$baseUrl/hentai_manga"

    override fun imageElement() = "table[align=\"center\"] > tbody > tr"

    override fun popularMangaSelector() = "div#main div#content.content div.wf-cell"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("article.post > div.blog-content a").let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.text()
        }
        return manga
    }

    override fun popularMangaNextPageSelector() = "div.page-nav > a.nav-next"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search/?s=$query", headers)
    }

    override fun searchMangaSelector() = "div#main div#content div.wf-cell"

    override fun searchMangaNextPageSelector() = "div.page-nav > a.nav-next"

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        val infoElement = document.select("article > center tbody")

        manga.thumbnail_url = document.select("article > center > a > img").first()?.attr("src")
        manga.status = SManga.UNKNOWN
        manga.artist = infoElement.select("tr:eq(1) > td > span").text().substringAfter("Artist: ")
        manga.genre = infoElement.select("tr:eq(4) > td > span").text().substringAfter("Categories: ")
        return manga
    }

    override fun chapterListSelector() = "center > p > span > strong > a:contains(View Thumbnails)"

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = ArrayList<SChapter>()
        SChapter.create().apply {
            this.setUrlWithoutDomain(response.asJsoup().select(chapterListSelector()).attr("href"))
            this.name = "No Title"
            chapters.add(this)
        }
        return chapters
    }

}
