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

import org.slf4j.{Logger => JLogger}
import cats.effect.Sync
import org.typelevel.log4cats._
import cats.{Applicative, Defer, Id}

// format: off
trait Slf4jGenLogging[G[_], F[_]] extends GenLogging[G, SelfAwareStructuredLogger[F]] {
  protected implicit def syncF: Sync[F]
  protected def lift(f: => SelfAwareStructuredLogger[F]): G[SelfAwareStructuredLogger[F]]

  override def fromName(name: String): G[SelfAwareStructuredLogger[F]] = lift(Slf4jLogger.getLoggerFromName[F](name))
  def fromSlf4j(logger: JLogger): G[SelfAwareStructuredLogger[F]] = lift(Slf4jLogger.getLoggerFromSlf4j[F](logger))
}
// format: off

object Slf4jGenLogging {
  def apply[G[_], F[_]](implicit logging: Slf4jGenLogging[G, F]): Slf4jGenLogging[G, F] = logging

  // format: off
  def forSync[G[_], F[_]](implicit F: Sync[F], G: Applicative[G], GD: Defer[G]): Slf4jGenLogging[G, F] =
    new Slf4jGenLogging[G, F] {
      override protected val syncF: Sync[F] = F
      override protected def lift(l: => SelfAwareStructuredLogger[F]): G[SelfAwareStructuredLogger[F]] = GD.defer(G.pure(l))
    }
  // format: on
}

trait Slf4jLogging[F[_]] extends Slf4jGenLogging[Id, F]

object Slf4jLogging {
  def apply[F[_]](implicit logging: Slf4jLogging[F]): Slf4jLogging[F] = logging

  // format: off
  def forSync[F[_]](implicit F: Sync[F]): Slf4jLogging[F] = new Slf4jLogging[F] {
    override protected val syncF: Sync[F] = F
    override protected def lift(f: => SelfAwareStructuredLogger[F]): Id[SelfAwareStructuredLogger[F]] = f

  }
  // format: on
}

trait Slf4jLoggingF[F[_]] extends Slf4jGenLogging[F, F]

object Slf4jLoggingF {
  def apply[F[_]](implicit logging: Slf4jLoggingF[F]): Slf4jLoggingF[F] = logging

  // format: off
  def forSync[F[_]](implicit F: Sync[F]): Slf4jLoggingF[F] = new Slf4jLoggingF[F] {
    override protected val syncF: Sync[F] = F
    override protected def lift(f: => SelfAwareStructuredLogger[F]): F[SelfAwareStructuredLogger[F]] = F.defer(F.pure(f))
  
  }
  // format: on
}
