package com.timushev.sbt.updates

import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import sbt.Logger

import java.io.InputStream
import java.net.URL

class Downloader(repositoryId: String, authentications: Seq[RepositoryAuthentication], logger: Logger) {
  def startDownload(url: URL): InputStream = {
    val hostAuthentication = RepositoryAuthentication.find(url.getHost, repositoryId, authentications)
    val connection         = url.openConnection()
    connection.setRequestProperty("User-Agent", "sbt-updates")
    // Otherwise Java sets a default that is not accepted by all remote repositories (AWS CodeArtifact as an example)
    connection.setRequestProperty("Accept", "*/*")
    hostAuthentication match {
      case Some(c) =>
        logger.debug(s"Downloading $url as ${c.describe}")
        if (c.user.nonEmpty || c.password.nonEmpty) {
          val auth = Base64.encodeToString(s"${c.user}:${c.password}".getBytes)
          connection.setRequestProperty("Authorization", s"Basic $auth")
        }
        c.headers.foreach { case (name, value) => connection.addRequestProperty(name, value) }
      case None =>
        logger.debug(s"Downloading $url anonymously")
    }
    connection.setConnectTimeout(120000)
    connection.setReadTimeout(120000)
    connection.getInputStream
  }
}
