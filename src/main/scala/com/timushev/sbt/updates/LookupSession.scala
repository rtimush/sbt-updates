package com.timushev.sbt.updates

import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import com.timushev.sbt.updates.versions.Version

import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}

/** Lookup state shared by all projects within one command, so that the next command fetches everything again. */
final class LookupSession {
  private val documents   = new ConcurrentHashMap[LookupSession.DocumentKey, Future[Seq[Version]]]()
  private val unreachable = new ConcurrentHashMap[String, Throwable]()

  def document(kind: String, url: URL, authentication: Option[RepositoryAuthentication])(
      fetch: => Future[Seq[Version]]
  ): Future[Seq[Version]] =
    documents.computeIfAbsent(
      LookupSession.DocumentKey(kind, url.toString, authentication),
      (_: LookupSession.DocumentKey) => fetch
    )

  def unreachableCause(url: URL): Option[Throwable] = Option(unreachable.get(LookupSession.origin(url)))

  def markUnreachable(url: URL, cause: Throwable): Unit = unreachable.putIfAbsent(LookupSession.origin(url), cause)
}

object LookupSession {
  final private case class DocumentKey(kind: String, url: String, authentication: Option[RepositoryAuthentication])

  /** Scheme, host and port of a URL without its user info, so that it can be logged. */
  def origin(url: URL): String = {
    val port = if (url.getPort == -1 || url.getPort == url.getDefaultPort) "" else s":${url.getPort}"
    s"${url.getProtocol}://${url.getHost}$port"
  }

  /** The URL without its user info, query or fragment, which can carry tokens, for messages and logs. */
  def redacted(url: URL): String = origin(url) + url.getPath

  // Lookups block on HTTP, so they get their own threads rather than the global pool, which is sized to the CPU count.
  implicit lazy val executionContext: ExecutionContext = {
    val threads                = new AtomicInteger
    val factory: ThreadFactory = (runnable: Runnable) => {
      val thread = new Thread(runnable, s"sbt-updates-lookup-${threads.incrementAndGet()}")
      thread.setDaemon(true)
      thread
    }
    val executor = new ThreadPoolExecutor(16, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable](), factory)
    executor.allowCoreThreadTimeOut(true)
    ExecutionContext.fromExecutorService(executor)
  }
}
