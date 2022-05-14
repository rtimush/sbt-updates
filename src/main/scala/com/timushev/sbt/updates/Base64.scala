package com.timushev.sbt.updates

object Base64 {
  private class Java678Encoder extends Array[Byte] => String {
    override def apply(bytes: Array[Byte]): String =
      javax.xml.bind.DatatypeConverter.printBase64Binary(bytes)
  }

  private class Java9Encoder extends Array[Byte] => String {
    override def apply(bytes: Array[Byte]): String =
      java.util.Base64.getEncoder.encodeToString(bytes)
  }

  private lazy val encoder: Array[Byte] => String =
    try {
      new Java678Encoder.apply(Array.emptyByteArray)
      new Java678Encoder
    } catch {
      case _: LinkageError => new Java9Encoder
    }

  def encodeToString(bytes: Array[Byte]): String = encoder(bytes)
}
