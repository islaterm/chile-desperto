package com.github.islaterm

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.dispatcher.text
import me.ivmg.telegram.extensions.filters.Filter
import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern

val urlPattern: Pattern = Pattern.compile(
    "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)

val runtime: Runtime = Runtime.getRuntime()

fun main() {
    val linksFile = File("links.txt")
    if (!linksFile.exists()) {
        linksFile.createNewFile()
    }
    val urls = File("links.txt").readLines().toMutableSet()
    val key = Yaml().load<Map<String, String>>(File("src/main/resources/token.yaml").readText())
    val bot = bot {
        token = key["token"] ?: error("")
        dispatch {
            message(Filter.All) { _, update ->
                val text = update.message?.text ?: update.message?.caption ?: ""
                val matcher = urlPattern.matcher(text)
                while (matcher.find()) {
                    val matchStart = matcher.start(1)
                    val matchEnd = matcher.end()
                    val link = text.substring(matchStart until matchEnd)
                    urls.add(link)
                }
                linksFile.writeText(urls.joinToString(System.lineSeparator()))
                var process = runtime.exec("git.exe status")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe commit -a -m \"Updated links\" ")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe pull origin master")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
                process = runtime.exec("git.exe push origin master")
                BufferedReader(InputStreamReader(process.inputStream)).lines().forEach { println(it) }
            }
        }
    }
    bot.startPolling()
}

