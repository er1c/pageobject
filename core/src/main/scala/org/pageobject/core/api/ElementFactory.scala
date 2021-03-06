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
package org.pageobject.core.api

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

case class ElementFactory(initial: WebElement, retry: () => WebElement)(implicit val webDriver: WebDriver)

object ElementFactory {
  def apply(factory: () => WebElement)(implicit webDriver: WebDriver): ElementFactory = {
    ElementFactory(factory(), factory)
  }
}
