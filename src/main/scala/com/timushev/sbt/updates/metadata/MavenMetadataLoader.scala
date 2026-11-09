package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.versions.Version
import com.timushev.sbt.updates.{Downloader, LookupSession}
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import sbt.{ModuleID, URLRepository}

import java.io.InputStream
import java.net.URI
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import scala.concurrent.Future
import scala.util.control.NonFatal

class MavenMetadataLoader(repo: URLRepository, downloader: Downloader, session: LookupSession) extends MetadataLoader {
  def repository: String = repo.name

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    MetadataLoader.fromDocuments(repo.patterns.artifactPatterns.flatMap(url(_, module)))(download)

  private def url(pattern: String, module: ModuleID): Option[String] =
    RepositoryPattern
      .revisionPrefix(pattern, module, module.organization.split("\\.").mkString("/"))
      .map(_ + "maven-metadata.xml")

  private def download(url: String): Future[Seq[Version]] = {
    val location = new URI(url).toURL
    session.document("maven-metadata", location, downloader.authentication(location)) {
      Future {
        val in = downloader.startDownload(location)
        try MavenMetadataLoader.extractVersions(in)
        finally in.close()
      }
    }
  }
}

object MavenMetadataLoader {
  private lazy val parserFactory: SAXParserFactory = {
    val factory = SAXParserFactory.newInstance()
    // Resolver metadata is untrusted input. Every JAXP parser supports secure processing; where the parser also supports
    // them, refuse DOCTYPEs and external entities outright, whatever the JVM defaults are.
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    Seq(
      "http://apache.org/xml/features/disallow-doctype-decl"           -> true,
      "http://xml.org/sax/features/external-general-entities"          -> false,
      "http://xml.org/sax/features/external-parameter-entities"        -> false,
      "http://apache.org/xml/features/nonvalidating/load-external-dtd" -> false
    ).foreach { case (feature, enabled) =>
      try factory.setFeature(feature, enabled)
      catch { case NonFatal(_) => () }
    }
    factory
  }

  private[metadata] def extractVersions(in: InputStream): Seq[Version] = {
    val versions = Vector.newBuilder[Version]
    val parser   = parserFactory.synchronized(parserFactory.newSAXParser())
    parser.parse(
      in,
      new DefaultHandler {
        private var path = List.empty[String]
        private val text = new java.lang.StringBuilder

        override def startElement(uri: String, localName: String, qName: String, attributes: Attributes): Unit = {
          path = qName.substring(qName.indexOf(':') + 1) :: path
          text.setLength(0)
        }

        override def characters(ch: Array[Char], start: Int, length: Int): Unit = text.append(ch, start, length)

        override def endElement(uri: String, localName: String, qName: String): Unit = {
          // <metadata><versioning><versions><version>, whatever the root element is called
          if (path.lengthCompare(4) == 0 && path.take(3) == List("version", "versions", "versioning"))
            versions += Version(text.toString.trim)
          path = path.tail
        }
      }
    )
    versions.result()
  }
}
