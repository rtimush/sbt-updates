package com.timushev.sbt.updates

import org.scalatest.freespec.AnyFreeSpec

class Base64Spec extends AnyFreeSpec {
  "Base64 encoder" - {
    "should encode data" in
      assert(Base64.encodeToString(Array[Byte](1, 127)) === "AX8=")
  }
}
