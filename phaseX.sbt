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

libraryDependencies ++= Seq("ws.securesocial" %% "securesocial" % "master-SNAPSHOT",
  "de.htwg.se.CardGame.PhaseX" % "PhaseX" % "1.0-SNAPSHOT",
  "com.google.code.gson" % "gson" % "2.5")

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "PhaseX_Nexus" at "http://nexus-phasex.rhcloud.com/content/groups/public/"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options")

includeFilter in (Assets, LessKeys.less) := "*.less"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

