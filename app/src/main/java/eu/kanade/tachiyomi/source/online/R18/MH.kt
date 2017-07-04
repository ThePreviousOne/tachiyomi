package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element

class MH : Base() {

    override val id: Long = 19L

    override val name: String = "MassiveHentai"

    override val baseUrl = "http://massivehentai.com"

    override fun pageUrl() = "$baseUrl/hentai_manga"

    override fun imageElement() = "table > tbody > tr"

    override fun popularMangaSelector() = "div#content > div#boxes > div.entry"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("h2.title > a").let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.text()
        }
        return manga
    }

    override fun popularMangaNextPageSelector() = "div#controllers > a"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("http://massivehentai.com/?s=$query", headers)
    }

    override fun searchMangaSelector() = "div#content > div#boxes > div.entry"

    override fun searchMangaNextPageSelector() = "div#controllers > a"

    override fun mangaDetailsParse(response: Response): SManga {
        val manga = SManga.create()
        response.asJsoup().select("div.post-content > div.post-text > center").let { el ->
            manga.thumbnail_url = el.select("a > img").attr("src")
            el.select("table > tbody > tr").let {
                manga.author = ".."
                manga.artist = it.select("tr:eq(1) > td > span").text().substringAfter("Artist: ")
                manga.status = SManga.UNKNOWN
                manga.genre = it.select("tr:eq(4) > td > span").text().substringAfter("Categories: ")
            }
        }
        return manga
    }

    override fun chapterListSelector() = "div.post-text > center > p > span > strong > a"

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        SChapter.create().apply {
            this.setUrlWithoutDomain(response.asJsoup().select(chapterListSelector()).attr("href"))
            this.name = "No Title"
            chapters.add(this)
        }
        return chapters
    }

}
