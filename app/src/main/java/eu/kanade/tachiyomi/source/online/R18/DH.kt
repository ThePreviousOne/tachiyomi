package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element

class DH : Base() {

    override val id: Long = 15L

    override val name: String = "DoujinshiHentai"

    override val baseUrl = "http://doujinshihentai.com"

    override fun pageUrl() = "$baseUrl/manga"

    override fun imageElement() = "table > tbody > tr"

    override fun popularMangaSelector() = "div#content div#posts > div"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        val e = element.select("div.entry > div > table > tbody")

        manga.thumbnail_url = e.select("tr:eq(0) > td:eq(0) > div > a > img").attr("src")
        manga.title = e.select("tr:eq(0) > td:eq(3)").text()
        manga.setUrlWithoutDomain(e.select("div.style5 > a:contains(View Thumbnails)").attr("href").substring(26))
        manga.artist = e.select("tr:eq(2) > td:eq(1) > div > a").text()
        manga.status = SManga.UNKNOWN
        manga.description = StringBuilder().append("Series: ").append(e.select("tr:eq(1) > td:eq(1) > div > div > a").text())
                .append("\n Translator: ").append(e.select("tr:eq(3) > td:eq(1) div > a").text()).toString()
        return manga
    }

    override fun popularMangaNextPageSelector() = "div.navigation > div.alignleft > a"

    override fun searchMangaSelector() = "div#content div#posts > div"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/?s=$query", headers)
    }

    override fun searchMangaNextPageSelector() = "div.navigation > div.alignleft > a"

    override fun mangaDetailsParse(response: Response): SManga {
        val manga = SManga.create()
        response.close()
        manga.author = ".."
        return manga
    }

    override fun chapterListSelector() = "table table table > tbody > tr"

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        val document = response.asJsoup()

        if (!document.select("div.weatimages_header").text().contains("Album not found")) {
            SChapter.create().apply {
                this.setUrlWithoutDomain(document.location())
                this.name = "No Title"
                chapters.add(this)
            }
        }
        return chapters
    }

}