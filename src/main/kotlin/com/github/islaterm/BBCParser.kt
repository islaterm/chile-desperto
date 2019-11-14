package com.github.islaterm

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.yaml.snakeyaml.Yaml
import java.io.File

private const val BBC_KEY = "bbc"

internal class BBCParser(private val updateHandler: UpdateHandler) {
  private var updates: MutableMap<String, Long>
  private val document: Document = Jsoup
      .connect("https://www.bbc.com/mundo/topics/f835c135-a54a-4670-8bfc-05938c5f6489")
      .get()
  private val yaml = Yaml()
  private val updatesFile = File("src/main/kotlin/resources/updates.yaml")

  init {
    if (!updatesFile.exists()) {
      updatesFile.createNewFile()
    }
    updates =
        yaml.load<MutableMap<String, Long>>(updatesFile.readText()) ?: mutableMapOf()
    if (!updates.contains(BBC_KEY)) {
      updates[BBC_KEY] = 0L
    }
  }

  internal fun parseUrls() {
    var links = ""
    val mainColumn = document.getElementsByClass("column--primary")[0]
    mainColumn.getElementsByClass("eagle-item__body").reversed().forEach {
      val pubDate =
          it.getElementsByClass("date date--v2")[0].attr("data-seconds").toLong()
      if (pubDate > updates[BBC_KEY]!!) {
        links += "https://www.bbc.com${it.getElementsByClass("title-link")[0].attr(
            "href"
        )}${System.lineSeparator()}"
        updates[BBC_KEY] = pubDate
      }
    }
    updatesFile.writeText(yaml.dump(updates))
    updateHandler.parseUpdate(links)
  }
}