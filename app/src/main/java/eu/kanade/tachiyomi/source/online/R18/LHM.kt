package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class LHM : Base() {

    override val id: Long = 18L

    override val name: String = "LoveHentaiManga"

    override val baseUrl = "http://lovehentaimanga.com"

    override fun pageUrl() = "$baseUrl/hentai_manga"

    override fun imageElement() = "table > tbody > tr"

    override fun popularMangaSelector() = "div#main div#content.content div.wf-cell"

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = ArrayList<SManga>()
        for (element in document.select(popularMangaSelector())) {
            val pattern = Pattern.compile("([A-Z])")
            if (!pattern.matcher(element.select("article.post > div.blog-content a").text()).find()) {
                mangas.add(popularMangaFromElement(element))
            }
        }

        val hasNextPage = popularMangaNextPageSelector()?.let { selector ->
            document.select(selector).first()?.absUrl("href")
        } != null

        return MangasPage(mangas, hasNextPage)
    }

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
        return GET("$baseUrl/?s=$query", headers)
    }

    override fun searchMangaSelector() = "div#main div#content div.wf-cell"

    override fun searchMangaNextPageSelector() = "div.page-nav > a.nav-next"

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        val infoElement = document.select("article > center tbody")

        if (!infoElement.isEmpty()) {
            manga.thumbnail_url = document.select("article > center > a > img").first()?.attr("src")
            manga.artist = infoElement.select("tr:eq(1) >td > span").text().substringAfter("Artist: ")
            manga.genre = infoElement.select("tr:eq(4) > td > span").text().substringAfter("Categories: ")
        } else {
            manga.thumbnail_url = document.select("article > p > a > img").first()?.attr("src")
            manga.author = ".."
        }
        manga.status = SManga.UNKNOWN
        return manga
    }

    override fun chapterListSelector() = "center > p > span > strong > a:contains(View Thumbnails)"

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = ArrayList<SChapter>()
        val document = response.asJsoup()

        SChapter.create().apply {
            if (document.select("div#content > article > center").isEmpty())
                this.setUrlWithoutDomain(document.select("article.post > p > a").attr("href"))
            else
                this.setUrlWithoutDomain(document.select(chapterListSelector()).attr("href"))
            this.name = "No Title"
            chapters.add(this)
        }
        return chapters
    }

}
