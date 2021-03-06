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
package org.pageobject.examples.wikipedia

import org.pageobject.core.page.PageModule

abstract class WikipediaPage(language: String, title: String)(entry: String)
  extends WikipediaDomainPage(language) {

  val path = s"/wiki/${entry.replaceAll(" ", "_")}"

  object content extends PageModule {
    private val firstHeading = $("#firstHeading")
  }

  object search extends WikipediaSearchModule

  override def atChecker() = pageTitle == s"$entry - $title"
}
