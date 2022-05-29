package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompoundMetadataLoader(loaders: Seq[MetadataLoader]) extends MetadataLoader {
  override def getVersions(module: sbt.ModuleID): Future[Seq[Version]] =
    Future
      .sequence {
        loaders.map { loader =>
          loader.getVersions(module).recover { case _ => Seq.empty }
        }
      }
      .map(_.flatten)
}
