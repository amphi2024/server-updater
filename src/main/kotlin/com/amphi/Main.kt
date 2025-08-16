package com.amphi

import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess

val serverDir = File(".")
fun main() {
    val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    try {
        // Check the latest server version
        val apiRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/amphi2024/server/releases/latest"))
            .build()
        val apiResponse = client.send(apiRequest, HttpResponse.BodyHandlers.ofString())
        if (apiResponse.statusCode() != 200) throw Exception("GitHub API failed: ${apiResponse.statusCode()}")

        val json = JSONObject(apiResponse.body())
        val latestVersion = json.getString("tag_name").removePrefix("v")
        val downloadUrl = "https://github.com/amphi2024/server/releases/download/v$latestVersion/server-$latestVersion.jar"

        println("Latest version detected: $latestVersion")

        // Backup the current server.jar
        val currentServer = File(serverDir, "server.jar")
        currentServer.takeIf { it.exists() }?.let {
            val versionedName = "server-${latestVersion}-old.jar"
            it.renameTo(File(serverDir, versionedName))
            println("Backed up existing server.jar to $versionedName")
        }

        // Download the latest server
        val tmpFile = File(serverDir, "server-$latestVersion.tmp.jar")
        val downloadRequest = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build()
        val downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray())

        if (downloadResponse.statusCode() != 200) throw Exception("Failed to download server.jar")
        FileOutputStream(tmpFile).use { it.write(downloadResponse.body()) }
        println("Downloaded latest server version to ${tmpFile.name}")

        // Replace old server.jar with the downloaded file
        tmpFile.renameTo(File(serverDir, "server.jar"))

        deleteObsolete()

        runServer()

        exitProcess(0)

    } catch (e: Exception) {
        e.printStackTrace()
        serverDir.listFiles { f -> f.name.endsWith("-old.jar") }?.firstOrNull()?.renameTo(File(serverDir, "server.jar"))

        runServer()
        exitProcess(1)
    }
}

fun deleteObsolete() {
    serverDir.listFiles { f -> f.name.endsWith("-old.jar") }?.forEach { oldJar ->
        oldJar.delete()
    }
}

fun runServer() {
    val javaHome = System.getProperty("java.home")
    val javaBin = File(javaHome, "bin/java").absolutePath
    ProcessBuilder(javaBin, "-jar", File(serverDir, "server.jar").absolutePath)
        .inheritIO()
        .start()
}