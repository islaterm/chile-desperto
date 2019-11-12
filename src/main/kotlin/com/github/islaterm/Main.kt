package com.github.islaterm

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.channel
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.Update
import me.ivmg.telegram.extensions.filters.Filter
import okhttp3.logging.HttpLoggingInterceptor
import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.concurrent.timer

val urlPattern: Pattern = Pattern.compile(
    "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)

val runtime: Runtime = Runtime.getRuntime()
val handler = UpdateHandler()
val urls = mutableSetOf<String>()
var lastUpdate: Long = System.currentTimeMillis()
val linksFile = File("links.txt")

fun main() {
    if (!linksFile.exists()) {
        linksFile.createNewFile()
    }
    val key = Yaml().load<Map<String, String>>(File("src/main/resources/token.yaml").readText())
    val bot = bot {
        logLevel = HttpLoggingInterceptor.Level.NONE
        token = key["token"] ?: error("")
        dispatch {
            message(Filter.All) { _, update ->
                handler.parseUpdate(update)
            }
            channel { _, update ->
                handler.parseUpdate(update)
            }
        }
    }
    timer("Git sync", period = 300_000L, )
    bot.startPolling()
}

class UpdateHandler {
    fun parseUpdate(update: Update) {
        synchronized(this) {
            val text = update.channelPost?.text ?: update.channelPost?.caption ?: ""
            val matcher = urlPattern.matcher(text)
            while (matcher.find()) {
                val matchStart = matcher.start(1)
                val matchEnd = matcher.end()
                val link = text.substring(matchStart until matchEnd)
                urls.add(link)
                println("Added $link")
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime > lastUpdate + 300_000) {
                lastUpdate = currentTime
                linksFile.writeText(urls.joinToString(System.lineSeparator()))
                var process = runtime.exec("git.exe status")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe commit -a -m \"Updated links\" ")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe pull origin master")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe push origin master")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                urls.clear()
            }
        }
    }
}

