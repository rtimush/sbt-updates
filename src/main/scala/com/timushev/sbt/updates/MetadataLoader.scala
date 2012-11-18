package com.timushev.sbt.updates

import sbt.{Resolver, MavenRepository, ModuleID}
import dispatch._
import versions.Version

object MetadataLoader {
  val factory: PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo)
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Promise[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository) extends MetadataLoader {

  def getVersions(module: ModuleID): Promise[Seq[Version]] =
    Http(metadataUrl(module) OK as.xml.Elem) map extractVersions

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) / "maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft url(repo.root))(_ / _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
