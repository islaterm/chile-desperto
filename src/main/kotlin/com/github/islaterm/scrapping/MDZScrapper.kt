package com.github.islaterm.scrapping

import com.github.islaterm.UpdateHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val UPDATE_KEY = "mdz"

internal class MDZScrapper(updateHandler: UpdateHandler) :
  AbstractScrapper(updateHandler) {
  override val baseUrl = "https://www.mdzol.com"
  private val topicLink = "$baseUrl/temas/chile-186.html"
  private val document: Document = Jsoup
      .connect(topicLink)
      .get()

  init {
    if (!updates.containsKey(UPDATE_KEY)) {
      updates[UPDATE_KEY] = 0L
    }
  }

  override fun parseSite() {
    var links = ""
    val newsData = document.getElementsByClass("news__data").forEach {
      val newsTitle = it.getElementsByTag("h2")[0]

    }
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}