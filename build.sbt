

name := "couchbase-java-sdk-in-scala"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "ReactiveCouchbase Releases" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases/"

resolvers += "ReactiveCouchbase repository" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "mvnrepository" at "http://mvnrepository.com/artifact/"

val projectMainClass = "cbtest.MyCouchBaseTest"

mainClass in (Compile, run) := Some(projectMainClass)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "io.reactivex" %% "rxscala" % "0.25.1",
  "com.couchbase.client" % "java-client" %"2.2.6"
)

