package com.github.islaterm

import com.github.islaterm.scrapping.BBCScrapper
import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.channel
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.extensions.filters.Filter
import okhttp3.logging.HttpLoggingInterceptor
import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimerTask
import java.util.regex.Pattern
import kotlin.concurrent.timer

private val urlPattern: Pattern = Pattern.compile(
    "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)
private val runtime: Runtime = Runtime.getRuntime()
// Map containing the
private val tokenMap: Map<String, String> =
    Yaml().load<Map<String, String>>(File("src/main/resources/token.yaml").readText())
private val handler = UpdateHandler()
private val urls = mutableSetOf<String>()
private val linksFile = File("links.txt")
private val bbcParser = BBCScrapper(handler)

/**
 * Main class of Chile Despertó Telegram bot.
 *
 * The bot collects all the links from @archivandochile's posts and from messages sent to
 * the bot via private messages or group chats.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.2.2
 */
class ChileDesperto {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      if (!linksFile.exists()) {
        linksFile.createNewFile()
      }
      val bot = setupBot()
      programScrapeTask()
      programSyncTask()
      bot.startPolling()
    }

    /**
     * Initializes de bot
     */
    private fun setupBot(): Bot = bot {
      token = tokenMap["token"] ?: error("")
      logLevel = HttpLoggingInterceptor.Level.NONE
      dispatch {
        message(Filter.All) { _, update ->
          val text = update.message?.text ?: update.message?.caption ?: ""
          handler.parseUpdate(text)
        }
        channel { _, update ->
          val text = update.channelPost?.text ?: update.channelPost?.caption ?: ""
          handler.parseUpdate(text)
        }
      }
    }

    /**
     * Creates a periodic task to fetch news from bbc
     */
    private fun programScrapeTask() {
      programTask("Scrape BBC", 3_600_000L, bbcParser::parseSite)
    }

    /**
     * Creates a periodic task to push new links to github.
     */
    private fun programSyncTask() {
      programTask("Git sync", 300_000L, handler::sync)
    }

    /**
     * Programs a task to execute an ``action`` every `period` milliseconds
     */
    private fun programTask(taskName: String, period: Long, action: () -> Unit) {
      timer(
          taskName, period = period,
          action = object : TimerTask(), (TimerTask) -> Unit {
            override fun run() {
              update()
            }

            override fun invoke(p1: TimerTask) {
              update()
            }

            private fun update() {
              val currentTime = System.currentTimeMillis()
              println("Executing $taskName")
              val format = SimpleDateFormat("dd-MM-yyyy HH:mm")
              val lastUpdate = Date(currentTime)
              val nextUpdate = Date(currentTime + period)
              println("Current time: ${format.format(lastUpdate)}")
              println("Next execution of $taskName: ${format.format(nextUpdate)}")
              action()
            }
          })
    }
  }
}

/**
 * Handler for the updates received by the bot.
 */
internal class UpdateHandler {

  /**
   * Checks if the message received contains urls and adds all of them to the url set.
   *
   * @param text
   *    the message received
   */
  internal fun parseUpdate(text: String) {
    synchronized(this) {
      val matcher = urlPattern.matcher(text)
      while (matcher.find()) {
        val matchStart = matcher.start(1)
        val matchEnd = matcher.end()
        val link = text.substring(matchStart until matchEnd)
        urls.add(link)
        println("Added $link")
      }
    }
  }

  /**
   * Syncs the changes with git
   */
  internal fun sync() {
    synchronized(this) {
      linksFile.appendText(urls.joinToString(System.lineSeparator()))
      if (urls.isNotEmpty()) {
        linksFile.appendText(System.lineSeparator())
        println("Pushing changes")
      }
      val os = System.getProperty("os.name")
      val process = when {
        os.contains("win", true) -> runtime.exec("powershell.exe .\\git-sync.ps1")
        else                     -> runtime.exec("sh git-sync.sh")
      }
      BufferedReader(InputStreamReader(process.errorStream)).lines()
          .forEach { println(it) }
      urls.clear()
    }
  }
}
