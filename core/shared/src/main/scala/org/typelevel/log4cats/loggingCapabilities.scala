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

package org.typelevel.log4cats

import com.lorandszakacs.enclosure.Enclosure

/**
 * Logging capability trait, or put crudeley "logger factory".
 *
 * The recommended way of creating loggers is through this capability trait.
 * You instantiate it once in your application (dependent on the specific
 * logging backend you use), and pass this around in your application.
 *
 * This has several advantages:
 *   - you no longer pass around _very powerful_ `F[_]: Sync` constraints that can do
 *     almost anything, when you just need logging
 *   - you are in control of how loggers are created, and you can even add in whatever
 *     custom functionality you need for your own applications here. e.g. create loggers
 *     that also send logs to some external providers by giving an implementation to this
 *     trait.
 *
 * @tparam F[_]
 *   the effect type in which the loggers are constructed.
 *   e.g. make cats.Id if logger creation can be done as a pure computation
 */
trait GenLogging[G[_], LoggerType] {
  def fromName(name: String): G[LoggerType]

  def create(implicit enc: Enclosure): G[LoggerType] = fromName(enc.fullModuleName)

  def fromClass(clazz: Class[_]): G[LoggerType] =
    fromName(clazz.getName) //N.B. .getCanonicalName does not exist on scala JS.
}

object GenLogging {
  def apply[G[_], LoggerType](implicit l: GenLogging[G, LoggerType]): GenLogging[G, LoggerType] =
    l
}

// N.B made it a trait, because otherwise on Scala 3 the implicitNotFound annotation would not be picked up :/
@scala.annotation.implicitNotFound(
  "Not found for Logging[${F}], keep in mind that, that ${F} is the effect in which logging is done. e.g. `Logging[${F}].create.info(message) : ${F}[Unit]`.\nAdditionally Logging[${F}] is defined as creating only loggers of type SelfAwareStructuredLogger[${F}].\nThe Logging[${F}] type itself described logging creation to be pure, i.e. cats.Id.\nSo your problem might be:\n\t1) you only have a GenLogger[G[_], ...] for some G[_] type other than cats.Id\n\t2) you do actually have a GenLogger[Id, L], but where L is not SelfAwareStructuredLogger[${F}].\nIf you are unsure how to create a Logging[${F}], then you need to look at the `log4cats-slf4j` module, or `log4cats-noop` for concrete implementations. Example for slf4j:\nimplicit val logging: Logging[IO] = Slf4jLogging.forSync[IO] // we create out Logging[IO]\nLogging[IO].create //we summon our instance, and create a SelfAwareStructuredLogger[${F}]."
)
trait Logging[F[_]] extends GenLogging[cats.Id, SelfAwareStructuredLogger[F]]

object Logging {
  def apply[F[_]](implicit l: Logging[F]): Logging[F] = l
}
