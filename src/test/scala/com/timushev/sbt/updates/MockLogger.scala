package com.timushev.sbt.updates

import sbt.Level

class MockLogger extends sbt.Logger {
  override def trace(t: => Throwable): Unit = ()
  override def log(level: Level.Value, message: => String): Unit = ()
  override def success(message: => String): Unit = ()
}
