package com.timushev.sbt.updates

import java.io.InputStream
import java.net.URL

import org.apache.ivy.Ivy
import sbt.{Credentials, Logger}

import scala.util.control.Exception._

class Downloader(credentials: Seq[Credentials], logger: Logger) {
  def startDownload(url: URL): InputStream = {
    val hostCredentials = nonFatalCatch.either(Credentials.forHost(credentials, url.getHost))
    val connection      = url.openConnection()
    // Same as in org.apache.ivy.util.url.BasicURLHandler
    connection.setRequestProperty("User-Agent", s"Apache Ivy/${Ivy.getIvyVersion}")
    hostCredentials match {
      case Right(Some(c)) =>
        logger.debug(s"Downloading $url as ${c.userName}")
        val auth = Base64.encodeToString(s"${c.userName}:${c.passwd}".getBytes)
        connection.setRequestProperty("Authorization", s"Basic $auth")
      case Right(None) =>
        logger.debug(s"Downloading $url anonymously")
      case Left(e) =>
        logger.debug(s"Downloading $url anonymously because credentials couldn't be loaded")
    }
    connection.setConnectTimeout(120000)
    connection.setReadTimeout(120000)
    connection.getInputStream
  }
}
