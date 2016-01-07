// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")
// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("play.version", "2.4.2"))

// Add Scalariform
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// PGP signing
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")