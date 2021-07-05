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

package org.typelevel.log4cats.noop.internal

import cats.Applicative
import org.typelevel.log4cats.noop._

trait NoOpLoggingInstances {
  implicit def log4catsSummonNoOpGenLogging[G[_]: Applicative, F[_]: Applicative]: NoOpLoggingGen[
    G,
    F
  ] = NoOpLoggingGen[G, F]
}
