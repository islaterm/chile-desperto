package com.github.islaterm.scrapping

import com.github.islaterm.UpdateHandler
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * This interface define the common and expected behaviour of scrappers for news websites
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.2.1
 */
internal interface IScrapper {

  /**
   * Search the site for news related to chilean protests
   */
  fun parseSite()
}

/**
 * This class contains the common behaviour of all scrappers.
 *
 * @property updateHandler
 *    the handler to which the parsed links are going to be sent.
 * @constructor
 *    Checks for the last update made by the scrappers and loads them as a dictionary.
 */
internal abstract class AbstractScrapper(protected val updateHandler: UpdateHandler) :
  IScrapper {

  protected var updates: MutableMap<String, Long>
  protected val updatesFile = File("src/main/kotlin/resources/updates.yaml")
  protected val yaml = Yaml()

  abstract val baseUrl: String

  init {
    if (!updatesFile.exists()) {
      updatesFile.createNewFile()
    }
    updates =
        yaml.load<MutableMap<String, Long>>(updatesFile.readText()) ?: mutableMapOf()
  }
}