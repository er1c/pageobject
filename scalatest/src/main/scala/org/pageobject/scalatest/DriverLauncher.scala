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
package org.pageobject.scalatest

import org.pageobject.core.browser.PageHolderWatcher
import org.pageobject.core.driver.DriverFactory
import org.pageobject.core.driver.DriverFactoryHolder
import org.pageobject.core.page.UnexpectedPagesFactory
import org.pageobject.core.tools.LimitProvider
import org.pageobject.core.tools.LogContext
import org.scalatest.Args
import org.scalatest.Status
import org.scalatest.Suite
import org.scalatest.SuiteMixin
import org.scalatest.WrapWith

/**
 * Use this trait if you want to test against different browsers
 *
 * When <code>runTest</code> and <code>runTests</code> are called in this test,
 * this calls are delegated to the <code>DriverFactory</code>
 * to create the driver instance if needed.
 */
@WrapWith(classOf[DriverLaunchWrapper])
trait DriverLauncher extends SuiteMixin with LimitProvider {
  this: Suite =>

  private val currentMock = DriverFactory.currentMock

  private val driverFactory = DriverFactoryHolder.value

  private val unexpectedPagesFactory = UnexpectedPagesFactory.option

  private val wrapperSuiteName = super.suiteName

  abstract override val suiteName = driverFactory.limit.name

  abstract override val suiteId = s"${super.suiteId}$$${driverFactory.limit.name}"

  override def limit = driverFactory.limit

  abstract override protected def runTests(testName: Option[String], args: Args): Status = {
    LogContext.apply(Map(
      LogContext.suiteName -> wrapperSuiteName,
      LogContext.browser -> suiteName
    )) {
      UnexpectedPagesFactory.withUnexpectedPages(unexpectedPagesFactory) {
        DriverFactoryHolder.withValue(Some(driverFactory)) {
          DriverFactory.withWebDriverMock(currentMock) {
            driverFactory.runTests(super.runTests(testName, args))
          }
        }
      }
    }
  }

  abstract override protected def runTest(testName: String, args: Args): Status = {
    LogContext(Map(
      LogContext.suiteName -> wrapperSuiteName,
      LogContext.browser -> suiteName,
      LogContext.testName -> testName
    )) {
      UnexpectedPagesFactory.withUnexpectedPages(unexpectedPagesFactory) {
        DriverFactoryHolder.withValue(Some(driverFactory)) {
          DriverFactory.withWebDriverMock(currentMock) {
            PageHolderWatcher.invalidateAfter() {
              driverFactory.runTest(testName, super.runTest(testName, args))
            }
          }
        }
      }
    }
  }
}
