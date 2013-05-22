package build

import sbt._
import Keys._

trait Settings {
    val buildSettings = Defaults.defaultSettings ++ CompileGroovyTask.settings ++ Seq(
        organization := "com.xyz",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.10.1"
    ) ++ Seq(
        libraryDependencies ++= dependencies
    )

    def dependencies = Dependencies.Compile ++ Dependencies.Test

    object Dependencies {
        val Compile = Nil

        val Test = "org.spockframework" % "spock-core" % "0.7-groovy-2.0" % "test" ::
            "org.gebish" % "geb-core" % "0.9.0" % "test" ::
            "org.gebish" % "geb-spock" % "0.9.0" % "test" ::
            "org.seleniumhq.selenium" % "selenium-java" % "2.32.0" % "test" ::
            "com.novocode" % "junit-interface" % "0.10-M4" % "test" ::
            Nil
    }
}

object Main extends Build with Settings {
    lazy val project = Project(
        id = "main",
        base = file("."),
        settings = buildSettings
    )
}
