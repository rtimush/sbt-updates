package com.timushev.sbt.updates

import java.io.InputStream
import java.net.URL

import sbt.{Credentials, Logger}

class Downloader(credentials: Seq[Credentials], logger: Logger) {

  def startDownload(url: URL): InputStream = {
    val hostCredentials = Credentials.forHost(credentials, url.getHost)
    val connection = url.openConnection()
    hostCredentials match {
      case Some(c) =>
        logger.debug(s"Downloading $url as ${c.userName}")
        val auth = Base64.encodeToString(s"${c.userName}:${c.passwd}".getBytes)
        connection.setRequestProperty("Authorization", s"Basic $auth")
      case None =>
        logger.debug(s"Downloading $url anonymously")
    }
    connection.getInputStream
  }

}
