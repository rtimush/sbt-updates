package com.timushev.sbt.updates

import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import sbt.Logger

import java.io.{FileNotFoundException, IOException, InputStream}
import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets.UTF_8

class Downloader(
    repositoryId: String,
    authentications: Seq[RepositoryAuthentication],
    logger: Logger,
    session: LookupSession
) {
  def authentication(url: URL): Option[RepositoryAuthentication] =
    RepositoryAuthentication.find(url.getHost, repositoryId, authentications)

  /** Fails with FileNotFoundException when the document doesn't exist, and with another IOException otherwise. */
  def startDownload(url: URL): InputStream = open(url, url, Downloader.MaxRedirects)

  private def open(url: URL, target: URL, redirectsLeft: Int): InputStream = {
    session.unreachableCause(target).foreach(cause => throw new RepositoryUnreachableException(target, cause))
    val connection = target.openConnection()
    connection.setRequestProperty("User-Agent", "sbt-updates")
    // Otherwise Java sets a default that is not accepted by all remote repositories (AWS CodeArtifact as an example)
    connection.setRequestProperty("Accept", "*/*")
    // Credentials belong to the requested origin: Java would forward custom headers such as X-Api-Key to any host.
    val credentials = if (LookupSession.origin(target) == LookupSession.origin(url)) authentication(url) else None
    credentials match {
      case Some(c) =>
        logger.debug(s"Downloading ${LookupSession.redacted(target)} as ${c.describe}")
        if (c.user.nonEmpty || c.password.nonEmpty) {
          val auth = Base64.encodeToString(s"${c.user}:${c.password}".getBytes(UTF_8))
          connection.setRequestProperty("Authorization", s"Basic $auth")
        }
        c.headers.foreach { case (name, value) => connection.addRequestProperty(name, value) }
      case None =>
        logger.debug(s"Downloading ${LookupSession.redacted(target)} anonymously")
    }
    connection.setConnectTimeout(Downloader.ConnectTimeoutMillis)
    connection.setReadTimeout(Downloader.ReadTimeoutMillis)
    // Skip the rest of this host's requests instead of letting each one time out. The message names the host that
    // actually failed, which after a redirect is not the repository's own.
    def unreachable(cause: IOException): Nothing = {
      session.markUnreachable(url, cause)
      session.markUnreachable(target, cause)
      throw new RepositoryUnreachableException(target, cause)
    }
    connection match {
      case http: HttpURLConnection =>
        http.setInstanceFollowRedirects(false)
        val status =
          try {
            http.connect()
            http.getResponseCode
          } catch { case e: IOException => unreachable(e) }
        if (status >= 200 && status < 300) http.getInputStream
        else {
          Downloader.discardBody(http)
          val location = Option(http.getHeaderField("Location")).filter(_ => Downloader.Redirects(status))
          location match {
            case Some(next) if redirectsLeft > 0 =>
              val resolved = target.toURI.resolve(next).toURL
              // Only http(s) is followed; a repository must not make the plugin read file: or jar: URLs.
              if (Downloader.Protocols(resolved.getProtocol)) open(url, resolved, redirectsLeft - 1)
              else throw new HttpStatusException(target, status)
            case _ if status == HttpURLConnection.HTTP_NOT_FOUND =>
              throw new FileNotFoundException(LookupSession.redacted(target))
            case _ => throw new HttpStatusException(target, status)
          }
        }
      case other =>
        try other.connect()
        catch { case e: IOException => unreachable(e) }
        other.getInputStream
    }
  }
}

object Downloader {
  val ConnectTimeoutMillis: Int = 15000
  val ReadTimeoutMillis: Int    = 120000
  val MaxRedirects: Int         = 5

  private val Redirects = Set(301, 302, 303, 307, 308)
  private val Protocols = Set("http", "https")

  private def discardBody(http: HttpURLConnection): Unit =
    try Option(http.getErrorStream).getOrElse(http.getInputStream).close()
    catch { case _: IOException => () }
}

final class HttpStatusException(val url: URL, val status: Int)
    extends IOException(s"HTTP $status for ${LookupSession.redacted(url)}")

final class RepositoryUnreachableException(val url: URL, cause: Throwable)
    extends IOException(
      s"could not reach ${LookupSession.origin(url)} (${Option(cause.getMessage).getOrElse(cause.getClass.getName)})",
      cause
    )
