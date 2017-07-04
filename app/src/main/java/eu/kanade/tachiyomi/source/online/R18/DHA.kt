package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element

class DHA : Base() {

    override val id: Long = 16L

    override val name: String = "DoujinsHentai-Alt"

    override val baseUrl = "http://doujinshihentai.com"

    override fun pageUrl() = "$baseUrl/manga"

    override fun imageElement() = "table > tbody > tr"

    override fun popularMangaSelector() = "table table table > tbody > tr"

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga/index.php/?albumspage=$page", headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val mangas = document.select(popularMangaSelector())
                .flatMap { it.select("td") }
                .filterNot { it.hasText() }
                .map {
                    popularMangaFromElement(it)
                }

        val hasNextPage = popularMangaNextPageSelector()?.let { selector ->
            document.select(selector).first()?.absUrl("href")
        } != null

        return MangasPage(mangas, hasNextPage)
    }

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("a").let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.select("img").attr("alt")
        }
        return manga
    }

    override fun popularMangaNextPageSelector() = "div.weatimages_pages_navigator > a:last-of-type"

    /** Just in-case */
    override fun searchMangaSelector() = "div#content div#posts > div"
    override fun searchMangaNextPageSelector() = "div.navigation > div.alignleft > a"
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/?s=$query", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val manga = SManga.create()
        val title = response.asJsoup().select("div.weatimages_toppest_navig").text().substring(24).replace(' ', '+')
        val element = client.newCall(searchMangaRequest(0, title, FilterList())).execute()
                .asJsoup().select("div#content div#posts > div > div.entry table")

        manga.thumbnail_url = element.select("tr:eq(0) > td:eq(0) > div > a > img").attr("src")
        manga.author = ".."
        manga.artist = element.select("tr:eq(2) > td:eq(1) > div > a").text()
        manga.status = SManga.UNKNOWN
        manga.description = StringBuilder().append("Series: ").append(element.select("tr:eq(1) > td:eq(1) > div > div > a").text())
                .append("\n Translator: ").append(element.select("tr:eq(3) > td:eq(1) div > a").text()).toString()
        return manga
    }

    override fun chapterListSelector() = "table table table > tbody > tr"

    override fun chapterListParse(response: Response): MutableList<SChapter> {
        val chapters = mutableListOf<SChapter>()
        val document = response.asJsoup()

        document.select(chapterListSelector())
                .flatMap { it.select("td") }
                .filterNot { it.hasText() }
                .forEach {
                    Chapter.create().apply {
                        this.name = it.select("a > img").attr("alt")
                        this.setUrlWithoutDomain(it.select("a").attr("href"))
                        chapters.add(this)
                    }
                }
        return chapters
    }

}