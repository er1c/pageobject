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

enablePlugins(SignedAetherPlugin)
overridePublishSignedSettings

// do not overload travis
concurrentRestrictions in Global := Seq(Tags.limitAll(1))

logBuffered in Test := false

val jettyVersion = "9.4.8.v20171121"
val seleniumVersion = "3.9.0"
val scalatestVersion = "3.0.1"
val slf4jVersion = "1.7.25"
val logbackVersion = "1.2.3"
val configVersion = "1.3.2"

lazy val commonSettings = Seq(
  organization := "org.pageobject",

  scalaVersion := sys.env.getOrElse("TRAVIS_SCALA_VERSION", "2.12.4"),

  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",

  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),

  homepage := Some(url("https://www.pageobject.org/")),

  publishMavenStyle := true,

  pomIncludeRepository := { _ => false },

  publishTo := {
    if ((version in ThisBuild).value.endsWith("SNAPSHOT")) {
      Some(Opts.resolver.sonatypeSnapshots)
    } else {
      Some(Opts.resolver.sonatypeStaging)
    }
  },

  pomExtra := // scalastyle:off xml.literal
    <scm>
      <url>https://github.com/agido/pageobject.git</url>
      <connection>scm:git:git@github.com:agido/pageobject.git</connection>
    </scm>
      <developers>
        <developer>
          <name>Dennis Rieks</name>
          <organization>agido GmbH</organization>
          <organizationUrl>https://www.agido.com/</organizationUrl>
        </developer>
      </developers>,
  // scalastyle:on xml.literal

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val noPublishSettings = commonSettings ++ Seq(
  publishLocal := {},
  publish := {}
)

lazy val root = (project in file("."))
  .settings(noPublishSettings: _*)
  .settings(
    name := "pageobject"
  )
  .aggregate(
    core,
    scalatest,
    jetty,
    test,
    examples
  )

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      // remove circular dependency
      "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion exclude("com.codeborne", "phantomjsdriver"),
      "org.seleniumhq.selenium" % "selenium-support" % seleniumVersion,
      "org.seleniumhq.selenium" % "htmlunit-driver" % "2.26" % Optional,
      "net.sourceforge.htmlunit" % "htmlunit" % "2.26" % Optional exclude("org.eclipse.jetty.websocket", "websocket-client"),
      "com.typesafe" % "config" % configVersion,

      // Warning: Class javax.annotation.Nullable not found
      "com.google.code.findbugs" % "jsr305" % "3.0.1" % Optional
    )
  )

lazy val scalatest = (project in file("scalatest"))
  .settings(commonSettings: _*)
  .settings(
    name := "scalatest",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % scalatestVersion,
      "org.pageobject.patch.org.scalatest" %% "scalatest" % scalatestVersion exclude("org.eclipse.jetty.orbit", "javax.servlet")
    )
  )
  .dependsOn(core)

lazy val jetty = (project in file("jetty"))
  .settings(noPublishSettings: _*)
  .settings(
    dependencyOverrides += "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion,

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
      "org.easymock" % "easymock" % "3.4",
      "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion,
      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
      "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
    )
  ).dependsOn(scalatest)

lazy val test = (project in file("test"))
  .settings(noPublishSettings: _*)
  .dependsOn(jetty)

lazy val examples = (project in file("examples"))
  .settings(noPublishSettings: _*)
  .dependsOn(jetty)

updateImpactOpenBrowser in ThisBuild := false

pgpPassphrase := sys.env.get("ENCRYPTION_PASSWORD").map(_.toArray)
pgpSecretRing := baseDirectory.value / ".gnupg" / "secring.gpg"
pgpPublicRing := baseDirectory.value / ".gnupg" / "pubring.gpg"
