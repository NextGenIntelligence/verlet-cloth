import com.typesafe.sbt.SbtStartScript

name := "verlet-cloth"

version := "0.0.1"

organization := "com.utexas.cs.sdao"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.jogamp.jogl" % "jogl-all-main" % "2.0.2",
  "org.jogamp.gluegen" % "gluegen-rt-main" % "2.0.2"
)

seq(SbtStartScript.startScriptForClassesSettings: _*)

SbtStartScript.stage in Compile := Unit

scalacOptions ++= Seq("-deprecation")
