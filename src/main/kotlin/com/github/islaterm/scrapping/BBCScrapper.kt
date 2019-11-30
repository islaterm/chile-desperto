package com.github.islaterm.scrapping


import com.github.islaterm.UpdateHandler
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val UPDATE_KEY = "bbc"

/**
 * This class searches for all the news about the Chilean protests in the BBC
 *
 * @property updateHandler
 *    the handler to which the parsed links are going to be sent.
 * @constructor
 *    Checks if the updates dictionary contains the last update for this parser and
 *    creates it if it doesn't exist.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.2.1
 */
internal class BBCScrapper(updateHandler: UpdateHandler) :
  AbstractScrapper(updateHandler) {

  override val baseUrl = "https://www.bbc.com"
  private val topicLink = "$baseUrl/mundo/topics/f835c135-a54a-4670-8bfc-05938c5f6489"
  private val document: Document = try {
    Jsoup
        .connect(topicLink)
        .get()
  } catch (ex: HttpStatusException) {
    Document("")
  }

  init {
    if (!updates.contains(UPDATE_KEY)) {
      updates[UPDATE_KEY] = 0L
    }
  }

  override fun parseSite() {
    var links = ""
    try {
      val mainColumn = document.getElementsByClass("column--primary")[0]
      mainColumn.getElementsByClass("eagle-item__body").reversed().forEach {
        val pubDate =
            it.getElementsByClass("date date--v2")[0].attr("data-seconds").toLong()
        if (pubDate > updates[UPDATE_KEY]!!) {
          links += "https://www.bbc.com${it.getElementsByClass("title-link")[0].attr(
              "href"
          )}${System.lineSeparator()}"
          updates[UPDATE_KEY] = pubDate
        }
      }
      updatesFile.writeText(yaml.dump(updates))
      updateHandler.parseUpdate(links)
    } catch (ex: IndexOutOfBoundsException) {
      println(ex.message)
    }
  }
}