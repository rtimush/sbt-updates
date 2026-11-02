package com.timushev.sbt.updates.authentication

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

// Same members as lm-coursier's Authentication and CoursierConfiguration, which are not on the test classpath.
class FakeAuthentication {
  def user: String                   = "user"
  def password: String               = "password"
  def realmOpt: Option[String]       = Some("realm")
  def headers: Seq[(String, String)] = Seq("X-Token" -> "token")
}

class FakeCoursierConfiguration {
  def authenticationByRepositoryId: Vector[(String, AnyRef)] = Vector("private" -> new FakeAuthentication)
}

class RepositoryAuthenticationSpec extends AnyFreeSpec with Matchers {
  "Repository authentication" - {
    "should be read from a Coursier configuration" in {
      RepositoryAuthentication.fromCoursier(new FakeCoursierConfiguration) shouldBe Seq(
        RepositoryAuthentication(Some("private"), Some("realm"), None, "user", "password", Seq("X-Token" -> "token"))
      )
    }
  }
}
