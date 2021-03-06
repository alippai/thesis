organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://nightlies.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"     % "1.2-20130516",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.2-M3"
)

seq(Revolver.settings: _*)