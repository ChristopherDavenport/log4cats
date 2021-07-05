/*
 * Copyright 2018 Christopher Davenport
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.log4cats.slf4j

import cats.effect._
import org.typelevel.log4cats._

class Slf4jLoggerSimpleClassMacroTest {
  def loggerF[F[_]: Sync]: F[SelfAwareStructuredLogger[F]] = Slf4jLogger.create[F]
  def logger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}

class Slf4jLoggerParameterizedClassMacroTest[A] {
  def loggerF[F[_]: Sync]: F[SelfAwareStructuredLogger[F]] = Slf4jLogger.create[F]
  def logger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}

class Slf4jLoggerHKTMacroTest[F[_]: Sync] {
  def loggerF: F[SelfAwareStructuredLogger[F]] = Slf4jLogger.create[F]
  def logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}

object Slf4jLoggerModuleMacroTest {
  def loggerF[F[_]: Sync]: F[SelfAwareStructuredLogger[F]] = Slf4jLogger.create[F]
  def logger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}

class Slf4jLoggerOuterClassMacroTest {
  class Slf4jLoggerInnerClassMacroTest {
    def loggerF[F[_]: Sync]: F[SelfAwareStructuredLogger[F]] = Slf4jLogger.create[F]
    def logger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
  }
}

object LoggingBaseline {
  val t = new Throwable
  def logger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val traceM = logger[IO].trace("")
  val traceTM = logger[IO].trace(t)("")
  val traceCM = logger[IO].trace(Map.empty[String, String])("")

  val debugM = logger[IO].debug("")
  val debugTM = logger[IO].debug(t)("")
  val debugCM = logger[IO].debug(Map.empty[String, String])("")

  val infoM = logger[IO].info("")
  val infoTM = logger[IO].info(t)("")
  val infoCM = logger[IO].info(Map.empty[String, String])("")

  val warnM = logger[IO].warn("")
  val warnTM = logger[IO].warn(t)("")
  val warnCM = logger[IO].warn(Map.empty[String, String])("")

  val errorM = logger[IO].error("")
  val errorTM = logger[IO].error(t)("")
  val errorCM = logger[IO].error(Map.empty[String, String])("")

}
