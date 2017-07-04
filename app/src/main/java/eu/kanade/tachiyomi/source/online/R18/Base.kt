package eu.kanade.tachiyomi.source.online.R18

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class Base() : ParsedHttpSource() {

    override val lang = "ja"

    override val supportsLatest = false

    abstract fun pageUrl(): String

    override fun popularMangaRequest(page: Int): Request {
        return GET(baseUrl, headers)
    }

    override fun searchMangaFromElement(element: Element): SManga {
        return popularMangaFromElement(element)
    }

    abstract fun imageElement(): String

    override fun pageListParse(response: Response): MutableList<Page> {
        val pages = mutableListOf<Page>()
        val imageE = imageElement()
        val imageSel = "a"
        val pageSel = "div.weatimages_pages_navigator"
        var loop = true
        var i = 0
        var doc = response.asJsoup()

        while (loop) {
            for (element in doc.select(imageE)) {
                for (e in element.select("td")) {
                    pages.add(Page(i, doc.location(), pageUrl()
                            + e.select(imageSel).attr("href").substringAfter("index.php").substringBefore('?')))
                    i++
                }
            }
            if (!doc.select(pageSel).isEmpty()) {
                val list = doc.select(pageSel).first().childNodes()
                if ((list.last() as Element).text().contains("›")) {
                    doc = client.newCall(Request.Builder().headers(headersBuilder().build())
                            .url(baseUrl + (list.last() as Element).attr("href")).build()).execute().asJsoup()
                } else loop = false
            } else loop = false
        }
        return pages
    }

    /** Removes unneeded functions from subclasses **/

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request { null!! }
    override fun searchMangaSelector() = ""
    override fun searchMangaNextPageSelector() = ""
    override fun latestUpdatesSelector(): String = ""
    override fun latestUpdatesNextPageSelector(): String? = ""
    override fun latestUpdatesFromElement(element: Element): SManga { return SManga.create() }
    override fun latestUpdatesRequest(page: Int): Request { TODO("not implemented") }
    override fun mangaDetailsParse(document: Document): SManga {null!!}
    override fun chapterFromElement(element: Element): SChapter {null!!}
    override fun pageListParse(document: Document): List<Page> {null!!}
    override fun imageUrlParse(document: Document) = ""


}