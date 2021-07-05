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

package org.typelevel.log4cats.slf4j.internal

import cats.effect.Sync
import cats.{Applicative, Defer}
import org.typelevel.log4cats.slf4j._

trait Slf4jLoggingInstances {
  implicit def log4catsSummonSLF4jLoggingForSync[F[_]: Sync]: Slf4jLogging[F] =
    Slf4jLogging.forSync[F]

  implicit def log4catsSummonSLF4jGenLoggingForSync[
      G[_]: Applicative: Defer,
      F[_]: Sync
  ]: Slf4jGenLogging[G, F] = Slf4jGenLogging.forSync[G, F]
}
