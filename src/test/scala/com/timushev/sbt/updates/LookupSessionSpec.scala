package com.timushev.sbt.updates

import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import com.timushev.sbt.updates.versions.Version
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CyclicBarrier, Executors, TimeUnit}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class LookupSessionSpec extends AnyFreeSpec with Matchers {
  private val url = new URI("https://repo.example/org/lib/maven-metadata.xml").toURL

  "A lookup session" - {
    "fetches a document once even when it is asked for concurrently" in {
      val callers = 8
      val pool    = Executors.newFixedThreadPool(callers)
      try {
        implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(pool)
        val session                       = new LookupSession
        val fetches                       = new AtomicInteger
        val barrier                       = new CyclicBarrier(callers)
        val results                       = (1 to callers).map { _ =>
          Future {
            barrier.await(5, TimeUnit.SECONDS)
            session.document("maven-metadata", url, None)(Future { fetches.incrementAndGet(); Seq(Version("1.0")) })
          }.flatten
        }
        results.foreach(result => Await.result(result, 5.seconds) shouldBe Seq(Version("1.0")))
        fetches.get shouldBe 1
      } finally pool.shutdown()
    }
    "keys documents by kind, URL and authentication" in {
      val session = new LookupSession
      val fetches = new AtomicInteger
      def fetch   = Future.successful { fetches.incrementAndGet(); Seq.empty[Version] }
      val auth    = Some(RepositoryAuthentication(None, None, None, "user", "secret", Nil))
      session.document("maven-metadata", url, None)(fetch)
      session.document("maven-metadata", url, None)(fetch)
      session.document("directory-listing", url, None)(fetch)
      session.document("maven-metadata", url, auth)(fetch)
      fetches.get shouldBe 3
    }
    "keeps user info out of origins and redacted URLs" in {
      val secret = new URI("https://user:secret@repo.example:8443/org/lib/maven-metadata.xml?token=t#f").toURL
      LookupSession.origin(secret) shouldBe "https://repo.example:8443"
      LookupSession.redacted(secret) shouldBe "https://repo.example:8443/org/lib/maven-metadata.xml"
      LookupSession.origin(new URI("https://repo.example:443/a").toURL) shouldBe "https://repo.example"
    }
  }
}
