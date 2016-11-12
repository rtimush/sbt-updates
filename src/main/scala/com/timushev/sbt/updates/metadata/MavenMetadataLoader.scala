package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version
import sbt.{MavenRepository, ModuleID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MavenMetadataLoader(repo: MavenRepository, downloadXML: String => Future[xml.Elem]) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    downloadXML(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root.stripSuffix("/")) (_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
