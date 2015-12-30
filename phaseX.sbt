name := "PhaseX"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

version := "2.4.4"

scalaVersion := "2.11.7"

scalariformSettings

libraryDependencies ++= Seq("ws.securesocial" %% "securesocial" % "master-SNAPSHOT")

resolvers += Resolver.sonatypeRepo("snapshots")

javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-encoding", "UTF-8", "-Xlint:-options")
//
//scalacOptions := Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature")
//
//routesImport ++= Seq("scala.language.reflectiveCalls")
