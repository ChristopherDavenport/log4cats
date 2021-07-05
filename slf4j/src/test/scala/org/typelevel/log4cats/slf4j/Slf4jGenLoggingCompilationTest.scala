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

import org.typelevel.log4cats._
import cats.effect._

object Slf4jGenLoggingCompilationTest {
  implicit val slf4jGenLogging: Slf4jGenLogging[IO, IO] = Slf4jGenLogging.forSync[IO, IO]
  implicit val slf4jLogging: Slf4jLogging[IO] = Slf4jLogging.forSync[IO]
  implicit val loggingF: LoggingF[IO] = slf4jGenLogging
  implicit val logging: Logging[IO] = slf4jLogging

  def justCompile: IO[Unit] = {
    for {
      _ <- slf4jGenLogging.create.flatMap(_.info("it compiled!"))
      _ <- Slf4jGenLogging[IO, IO].create.flatMap(_.info("it compiled!"))
      _ <- slf4jLogging.create.info("it compiled!")
      _ <- Slf4jLogging[IO].create.info("it compiled!")
    } yield ()
  }

}
