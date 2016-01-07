name := "PhaseX"

lazy val root = (project in file(".")).enablePlugins(PlayJava).enablePlugins(SbtWeb)

version := "2.4.4"

scalaVersion := "2.11.7"

scalariformSettings

libraryDependencies ++= Seq(
javaJdbc,
cache,
javaWs
)

libraryDependencies += "com.google.code.gson" % "gson" % "2.5"

libraryDependencies ++= Seq("ws.securesocial" %% "securesocial" % "master-SNAPSHOT")

resolvers += Resolver.sonatypeRepo("snapshots")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options")
