package com.timushev.sbt.updates

import sbt.{Resolver, MavenRepository, ModuleID}
import versions.Version
import scalaz.concurrent._
import java.net.URL
import scala.xml.XML

object MetadataLoader {
  val factory: PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo)
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Task[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository) extends MetadataLoader {

  def getVersions(module: ModuleID): Task[Seq[Version]] =
    Task(XML.load(new URL(metadataUrl(module)))).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root)(_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
