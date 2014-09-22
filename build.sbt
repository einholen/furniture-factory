organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-encoding", "utf8")

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= {
  val akkaV = "2.3.5"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"      % sprayV,
    "io.spray"            %%  "spray-routing"  % sprayV,
    "io.spray"            %%  "spray-json"     % "1.2.6",
    "io.spray"            %%  "spray-testkit"  % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV   % "test",
    "org.scalatest"       %%  "scalatest"      % "2.2.1" % "test"
  )
}

Revolver.settings