package com.timushev.sbt.updates.authentication

import com.timushev.sbt.updates.CompatUtils
import sbt._

object Coursier {

  type Authentication = {
    def user: String
    def password: String
    def realmOpt: Option[String]
    def headers: Seq[(String, String)]
  }

  type CoursierConfiguration = {
    def authenticationByRepositoryId: Vector[(String, Authentication)]
  }

  def dependencyUpdatesCsrConfigurationTask: Def.Initialize[Task[Option[CoursierConfiguration]]] =
    CompatUtils.optionalTask[CoursierConfiguration]("csrConfiguration")

}
