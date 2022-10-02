package com.timushev.sbt.updates

package object model {

  sealed trait ReportType
  case object SbtOutput extends ReportType
  case object Csv       extends ReportType
}
