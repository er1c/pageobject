/*
 * Copyright 2016 agido GmbH
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
// scalastyle:ignore header.matches
package org.scalatest

import java.util.concurrent.ExecutorService

import org.scalatest.tools.ConcurrentDistributor

object PageObjectHelper {
  def failedStatus(throwable: Throwable): Status = {
    // TODO not reported?
    // see scalatest issue #940
    // scalatest will ignore this exception...
    // see this comment in org.scalatest.tools.SuiteRunner line 61:
    // Don't forward the unreportedException to the returned status, because reporting it here in this SuiteAborted
    val result = new StatefulStatus
    result.setFailedWith(throwable)
    result.setCompleted()
    result
  }

  def suiteName(clazz: Class[_]): String = {
    Suite.stripDollars(Suite.parseSimpleName(clazz.getName))
  }

  def concurrentDistributor(args: Args, executorService: ExecutorService): ConcurrentDistributor = {
    new ConcurrentDistributor(args, executorService)
  }
}
