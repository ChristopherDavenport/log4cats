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

package org.typelevel

package object log4cats {

  /**
   * Convenience type for describing Logging where creation of the loggers itself is a pure operation
   *
   * @tparam F[_]
   *  the effect type in which logging is done.
   */
  @scala.annotation.implicitNotFound(
    """
Implicit not found for Logging[${F}]. Which is a convenience type alias.

If you are unsure how to create a Logging[${F}], then you can to look at the `log4cats-slf4j`, 
or `log4cats-noop` modules for concrete implementations.

Quickest fix might be to import the implicits:

    ```
    // assumes dependency on log4cats-slf4j module
    import org.typelevel.log4cats._
    import org.typelevel.log4cats.slf4j.implicits._
    val logger: SelfAwareStructuredLogger[IO] = Logging[IO].create
    // or:
    def anyFSyncLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Logging[F].create
    ```

Alternatively, a mutually exclusive solution, is to explicitely create your own Logging[F] instances
and pass them around implicitely:

    ```
    import cats.effect.IO
    import org.typelevel.log4cats._
    import org.typelevel.log4cats.slf4j._

    // we create our Logging[IO]
    implicit val logging: Logging[IO] = Slf4jLogging.forSync[IO]

    //we summon our instance, and create our logger
    val logger: SelfAwareStructuredLogger[IO] = Logging[IO].create
    logger.info("logging in IO!"): IO[Unit]

    def useLogging[F[_]: Logging] = Logging[F].create.info("yay! effect polymorphic code")

    useLogging[IO]
    ```

If neither of those things work, keep in mind that:
a) for the type Logging, ${F} is _only_ the effect in which logging is done.
    ```
    Logging[${F}].create.info(message) : ${F}[Unit]
    ```

b) The Logging[${F}] type itself described logging creation as being pure, i.e. in cats.Id

c) Logging[${F}] is defined as creating _only_ loggers of type SelfAwareStructuredLogger[${F}].

The full definition of Logging is:
    ```
    type Logging[F[_]] = GenLogging[cats.Id, SelfAwareStructuredLogger[F]]
    ```

Therefore, your problem might be:
    1) you `import org.typelevel.log4cats.slf4j.implicits._` AND created your own instance. Do one or the either.
    2) you only have a GenLogger[G[_], SelfAwareStructuredLogger[${F}]] for some G[_] type other than cats.Id
    3) you do actually have a GenLogger[Id, L], but where L is not SelfAwareStructuredLogger[${F}].
"""
  )
  type Logging[F[_]] = GenLogging[cats.Id, SelfAwareStructuredLogger[F]]

  /**
   * Convenience type
   *
   * @tparam F[_]
   *  the effect type of both logger creation, and logging.
   */
  @scala.annotation.implicitNotFound(
    """
Implicit not found for LoggingF[${F}]. Which is a convenience type alias.

If you are unsure how to create a LoggingF[${F}], then you can to look at the 
`log4cats-slf4j` module for concrete implementations.

Quickest fix might be to import the implicits:

    ```
    // assumes dependency on log4cats-slf4j module
    import org.typelevel.log4cats._
    import org.typelevel.log4cats.slf4j.implicits._
    import cats.effect.{Sync, IO}
    
    //we summon our instance, and create our logger
    val loggerIO: IO[SelfAwareStructuredLogger[IO]] = LoggingF[IO].create
    
    def anyFSyncLogger[F[_]: Sync]: F[SelfAwareStructuredLogger[F]] = LoggingF[F].create
    ```

Alternatively, a mutually exclusive solution, is to explicitely create your own LoggingF[F] instances
and pass them around implicitely:
    
    ```
    // assumes dependency on log4cats-slf4j module
    import org.typelevel.log4cats._
    import org.typelevel.log4cats.slf4j._
    import cats.effect.{Sync, IO}

    // we use IO for both logger creation effect, and logging effect. Otherwise it wouldn't be a LoggingF
    implicit val loggingF: LoggingF[IO] = Slf4jGenLogging.forSync[IO, IO]

    //we summon our instance, and create our logger
    val loggerIO: IO[SelfAwareStructuredLogger[IO]] = LoggingF[IO].create

    loggerIO.flatMap{logger => logger.info("logging in IO"): IO[Unit]}: IO[Unit]

    def useLogging[F[_]: LoggingF] = LoggingF[F].create.flatMap{logger => logger.info("yay! effect polymorphic code")}
    
    useLogging[IO]
    ```

If neither of those things work, keep in mind that:
a) for the type LoggingF, ${F} is _both_ the effect in which logger creation is done
   _and_ the effect in which logging is done
    ```
    val loggerF: ${F}[SelfAwareStructuredLogger] = Logging[${F}].create
    loggerF.flatMap{logger => logger.info("the result of the .info is in F"): ${F}[Unit] }: ${F}[Unit]
    ```

b) LoggingF[${F}] is defined as creating _only_ loggers of type SelfAwareStructuredLogger[${F}].

The full definition of Logging is:
    ```
    type LoggingF[F[_]] = GenLogging[F, SelfAwareStructuredLogger[F]]
    ```
Therefore, your problem might be:
    1) you `import org.typelevel.log4cats.slf4j.implicits._` AND created your own instance. Do one or the either.
    2) you only have a GenLoggerF[G[_], SelfAwareStructuredLogger[${F}]] for some G[_] type other than type ${F}
    3) you do actually have a GenLogger[${F}, L], but where L is not SelfAwareStructuredLogger[${F}].
"""
  )
  type LoggingF[F[_]] = GenLogging[F, SelfAwareStructuredLogger[F]]

}
