import CiCommands.{ ciBuild, devBuild }
import scoverage.ScoverageKeys.coverageFailOnMinimum

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "doobie-hikari-testkit",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.9",
      "org.scalamock" %% "scalamock" % "4.4.0",
      "org.typelevel" %% "cats-effect" % "2.3.0",
      "org.tpolecat" %% "doobie-hikari" % "0.13.4"
    ),
    commands ++= Seq(ciBuild, devBuild),
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true
  )
