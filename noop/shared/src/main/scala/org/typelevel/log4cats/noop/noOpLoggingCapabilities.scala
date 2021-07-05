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

package org.typelevel.log4cats.noop

import cats._
import org.typelevel.log4cats._

trait NoOpLoggingGen[G[_], F[_]] extends GenLogging[G, SelfAwareStructuredLogger[F]]
trait NoOpLogging[F[_]] extends NoOpLoggingGen[cats.Id, F]
trait NoOpLoggingF[F[_]] extends NoOpLoggingGen[F, F]

object NoOpLoggingGen {
  def apply[G[_], F[_]](implicit logging: NoOpLoggingGen[G, F]): NoOpLoggingGen[G, F] = logging

  implicit def create[G[_]: Applicative, F[_]: Applicative]: NoOpLoggingGen[G, F] =
    new NoOpLoggingGen[G, F] {
      override def fromName(name: String): G[SelfAwareStructuredLogger[F]] =
        Applicative[G].map(Applicative[G].pure(name))(_ => NoOpLogger[F])
    }
}

object NoOpLogging {
  def apply[F[_]](implicit l: NoOpLogging[F]): NoOpLogging[F] = l

  def create[F[_]: Applicative]: NoOpLogging[F] = new NoOpLogging[F] {
    override def fromName(name: String): SelfAwareStructuredLogger[F] =
      Applicative[Id].map(Applicative[Id].pure(name))(_ => NoOpLogger[F])
  }
}

object NoOpLoggingF {
  def apply[F[_]](implicit l: NoOpLoggingF[F]): NoOpLoggingF[F] = l

  def create[F[_]: Applicative]: NoOpLoggingF[F] = new NoOpLoggingF[F] {
    override def fromName(name: String): F[SelfAwareStructuredLogger[F]] =
      Applicative[F].map(Applicative[F].pure(name))(_ => NoOpLogger[F])
  }
}
