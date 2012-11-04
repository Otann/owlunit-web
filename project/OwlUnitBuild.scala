import sbt._
import sbt.Keys._
import sbt.Package._
import java.util.jar.Attributes.Name._
import com.github.siasia.WebPlugin.webSettings
import com.github.siasia.PluginKeys._
import com.untyped.sbtjs.Plugin.{JsKeys, jsSettings}
import com.typesafe.startscript.StartScriptPlugin

object OwlUnitBuild extends Build {

  lazy val buildSettings = Seq(
    organization := "com.owlunit",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.9.1"
  )

  lazy val root = Project(
    id = "ou",
    base = file("."),
    settings = defaultSettings,
    aggregate = Seq(crawl, web)
  )

  lazy val crawl = Project(
    id = "ou-crawl",
    base = file("ou-crawl"),
    dependencies = Seq(web),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.slf4s, Dependency.logback, Dependency.iiCore),
      mainClass in Compile := Some("com.owlunit.crawl.Crawler")
    ) ++ StartScriptPlugin.startScriptForClassesSettings
  )

  lazy val web = Project(
    id = "ou-web",
    base = file("ou-web"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.lift
        ++ Dependencies.webPlugin
        ++ Seq(Dependency.iiCore, Dependency.jQuery, Dependency.specs2),
      scanDirectories in Compile := Nil, // For JRebel
      mainClass in Compile := Some("JettyLauncher")
    ) ++ webSettings ++ jsSettings ++ coffeeSettings ++ StartScriptPlugin.startScriptForClassesSettings
  )

  /////////////////////
  // Settings
  /////////////////////

  seq(webSettings: _*)

  override lazy val settings = super.settings ++ buildSettings ++ Seq(
  	shellPrompt := { s => Project.extract(s).currentProject.id + "> " }
  )

  lazy val coffeeSettings = Seq(
    // To change the directory that is scanned, use:
    (sourceDirectory in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "coffee"),

    // To change the destination directory to src/main/webapp in an xsbt-web-plugin project, use:
    (resourceManaged in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "webapp" / "static" / "js" / "coffee"),

    // To automatically add generated Javascript files to the application JAR:
    (resourceGenerators in Compile) <+= (JsKeys.js in Compile),

    // To cause the js task to run automatically when you run compile:
    (compile in Compile) <<= (compile in Compile) dependsOn (JsKeys.js in Compile),

    // To use pretty-printing instead of regular Javascript minification:
    (JsKeys.prettyPrint in (Compile, JsKeys.js)) := true
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ buildSettings ++ Seq(
    resolvers ++= Seq(Resolvers.liftModules),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked"),
    publishTo := Some(Resolver.file("file",  new File( "/Users/anton/Dev/Owls/repo/owlunit.github.com/repo/ivy/" )) ),
    StartScriptPlugin.stage in Compile := Unit,

//    parallelExecution in Test := false // runs all tests sequentially
    testOptions in Test += Tests.Setup( () => System.setProperty("run.mode", "test") ),
    testOptions in Test += Tests.Cleanup( () => System.setProperty("run.mode", "development") )

  )


  /////////////////////
  // Dependencies
  /////////////////////
  
  object Resolvers {
    
    val owlUnitIvy  = "OwlUnit Ivy Repo" at "http://owlunit.github.com/repo/ivy"
    val owlUnitM2   = "OwlUnit Maven2 Repo" at "http://owlunit.github.com/repo/m2"
    val liftModules = "Liftmodules repo" at "https://repository-liftmodules.forge.cloudbees.com/release"

  }
  
  object Dependencies {

    val lift = Seq(
      Dependency.liftWebKit,
      Dependency.liftWizard,
      Dependency.liftWidgets,
      Dependency.liftMongo,
      Dependency.liftJson,
      Dependency.dispatch,
      Dependency.auth,
      Dependency.rogue,
      Dependency.logback
    )

    val webPlugin = Seq(
      Dependency.jettyWebapp % "container",
      Dependency.jettyWebapp % "compile->default",
      Dependency.jettyServer % "compile->default",
      Dependency.servlet % "compile->default"
    )

  }

  object Dependency {

    // Versions

    object V {
      val Lift      = "2.4"
      val Jetty     = "7.3.1.v20110307"
    }

    // Dependencies

    val iiCore      = "com.owlunit"               %% "core"                % "0.3-SNAPSHOT"

    val spring      = "org.springframework"       %  "spring-context"      % "3.0.5.RELEASE"
    val dispatch    = "net.databinder"            %% "dispatch-http"       % "0.8.8"
    val jQuery      = "net.liftmodules"           %% "lift-jquery-module"  % (V.Lift + "-1.0")

    val liftWebKit  = "net.liftweb"               %% "lift-webkit"         % V.Lift    % "compile->default"
    val liftWizard  = "net.liftweb"               %% "lift-wizard"         % V.Lift    % "compile->default"
    val liftWidgets = "net.liftweb"               %% "lift-widgets"        % V.Lift    % "compile->default"
    val liftJson    = "net.liftweb"               %% "lift-json"           % V.Lift    % "compile->default"
    val liftMongo   = "net.liftweb"               %% "lift-mongodb-record" % V.Lift    % "compile->default"
    val rogue       = "com.foursquare"            %% "rogue"               % "1.1.6"   intransitive()
    val auth        = "net.liftmodules"           %% "mongoauth"           % (V.Lift + "-0.3")

    val jettyWebapp = "org.eclipse.jetty"         %  "jetty-webapp"        % V.Jetty
    val jettyServer = "org.eclipse.jetty"         %  "jetty-server"        % V.Jetty
    val servlet     = "org.eclipse.jetty"         %  "jetty-servlet"       % V.Jetty

    val logback     = "ch.qos.logback"            %  "logback-classic"     % "1.0.6"
    val slf4s       = "com.weiglewilczek.slf4s"   %% "slf4s"               % "1.0.7"

    val specs2      = "org.specs2"                %% "specs2"               % "1.9"    % "test"

  }

}

