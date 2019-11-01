package com.timushev.sbt.updates

import org.scalatest.FreeSpec

class Base64Spec extends FreeSpec {
  "Base64 encoder" - {
    "should encode data" in {
      assert(Base64.encodeToString(Array[Byte](1, 127)) === "AX8=")
    }
  }
}
