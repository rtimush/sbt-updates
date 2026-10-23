package com.timushev.sbt.updates.upgrade

import java.io.File

/** A build source file (e.g. `build.sbt`, `Dependencies.scala`)
  * @param file
  *   source file
  * @param text
  *   file content
  * @param path
  *   file presented to the user.
  */
final case class SourceFile(file: File, text: String, path: String) {
  def lineOf(offset: Int): Int = text.substring(0, math.min(offset, text.length)).count(_ == '\n') + 1
  def isSbt: Boolean           = file.getName.endsWith(".sbt")
}

object SourceFile {
  def apply(file: File, text: String): SourceFile = SourceFile(file, text, file.getPath)
}
