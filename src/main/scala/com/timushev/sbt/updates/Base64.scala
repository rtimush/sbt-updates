package com.timushev.sbt.updates

object Base64 {
  private val encoder: Array[Byte] => String =
    bytes => java.util.Base64.getEncoder.encodeToString(bytes)

  def encodeToString(bytes: Array[Byte]): String = encoder(bytes)
}
