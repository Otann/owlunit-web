package ii

import sbt._
import Keys._
import sbt.Package._
import java.util.jar.Attributes.Name._
import com.github.siasia.WebPlugin._
import com.github.siasia.PluginKeys._
import com.typesafe.startscript.StartScriptPlugin

object OwlBuild extends Build {

  lazy val buildSettings = Seq(
    organization := "com.owlunit",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.9.1"
  )

  lazy val root = Project(
    id = "ii",
    base = file("."),
    settings = defaultSettings,
    aggregate = Seq(core, service, crawl, web)
  )

  lazy val core = Project(
    id = "ii-core",
    base = file("ii-core"),
    settings = defaultSettings ++ Seq(
      resolvers ++= Seq(
        "Cassandra adapter repository"     at "http://mvn.riptano.com/content/repositories/public",
        "Scale7 Maven Repo"                at "https://github.com/s7/mvnrepo/raw/master",
        "Neo4j Maven 2 release repository" at "http://m2.neo4j.org/releases"
      ),
      libraryDependencies ++= Dependencies.db
    )
  )

  lazy val service = Project(
    id = "ii-service",
    base = file("ii-service"),
    settings = defaultSettings ++ Seq(
      compileOrder := CompileOrder.Mixed
    ),
    dependencies = Seq(core)
  )

  lazy val crawl = Project(
    id = "ii-crawl",
    base = file("ii-crawl"),
    settings = defaultSettings ++ StartScriptPlugin.startScriptForClassesSettings ++ Seq(
      libraryDependencies ++= Seq(Dependency.spring, Dependency.logging),
      mainClass in Compile := Some("com.owlunit.crawl.Crawler")
    ),
    dependencies = Seq(core, service)

  )

  lazy val web = Project(
    id = "ii-web",
    base = file("ii-web"),
    settings = defaultSettings ++ webSettings ++ StartScriptPlugin.startScriptForClassesSettings ++ Seq(
      libraryDependencies ++= Dependencies.lift ++ Dependencies.webPlugin,
      scanDirectories in Compile := Nil,
      mainClass in Compile := Some("JettyLauncher")
    ),
    dependencies = Seq(core, service)
  )

  /////////////////////
  // Settings
  /////////////////////

  seq(webSettings: _*)
  seq(StartScriptPlugin.startScriptForClassesSettings: _*)

  // For JRebel
//  scanDirectories in Compile := Nil //WTF why this does not compile?

  override lazy val settings = super.settings ++ Seq(
  	shellPrompt := { s => Project.extract(s).currentProject.id + "> " }
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.owlunit",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.9.0",
    resolvers ++= Seq(ScalaToolsSnapshots, Resolvers.logging, Resolvers.owlUnitIvy, Resolvers.owlUnitM2),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked"),
    publishTo := Some(Resolver.file("file",  new File( "/Users/anton/Dev/Owls/repo/owlunit.github.com/repo/ivy/" )) ),
    StartScriptPlugin.stage in Compile := Unit
  )


  /////////////////////
  // Dependencies
  /////////////////////
  
  object Resolvers {
    
    val logging = "repo.codahale.com" at "http://repo.codahale.com"

    val owlUnitIvy = "OwlUnit Ivy Repo" at "http://owlunit.github.com/repo/ivy"
    val owlUnitM2  = "OwlUnit Maven2 Repo" at "http://owlunit.github.com/repo/m2"
    
  }
  
  object Dependencies {

    val db = Seq(Dependency.hector, Dependency.neo4j, Dependency.neo4jREST)
    
    val lift = Seq(
      Dependency.liftMapper,
      Dependency.liftWebKit,
      Dependency.liftWizard,
      Dependency.liftWidgets,
      Dependency.liftMongo,
      Dependency.auth
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
      val Neo4j     = "1.6"
      val Hector    = "0.8.0-2"
      val Jetty     = "7.3.1.v20110307"
    }

    // Compile

    val neo4j       = "org.neo4j"                 %  "neo4j"               % V.Neo4j
    val neo4jREST   = "org.neo4j"                 %  "neo4j-rest-graphdb"  % "1.7-SNAPSHOT"
    val hector      = "me.prettyprint"            %  "hector-core"         % V.Hector

    val spring      = "org.springframework"       %  "spring-context"      % "3.0.5.RELEASE"
    val dispatch    = "net.databinder"            %% "dispatch-http"       % "0.8.8"

    val liftWebKit  = "net.liftweb"               %% "lift-webkit"         % V.Lift    % "compile->default"
    val liftMapper  = "net.liftweb"               %% "lift-mapper"         % V.Lift    % "compile->default"
    val liftWizard  = "net.liftweb"               %% "lift-wizard"         % V.Lift    % "compile->default"
    val liftWidgets = "net.liftweb"               %% "lift-widgets"        % V.Lift    % "compile->default"
    val liftJson    = "net.liftweb"               %% "lift-json"           % V.Lift    % "compile->default"
    val liftMongo   = "net.liftweb"               %% "lift-mongodb-record" % V.Lift    % "compile->default"
    val rogue       = "com.foursquare"            %% "rogue"               % "1.1.6"   intransitive()
    val auth        = "net.liftmodules"           %% "mongoauth"           % (V.Lift + "-0.3")

    val jettyWebapp = "org.eclipse.jetty"         %  "jetty-webapp"        % V.Jetty
    val jettyServer = "org.eclipse.jetty"         %  "jetty-server"        % V.Jetty
    val servlet     = "org.eclipse.jetty"         %  "jetty-servlet"       % V.Jetty
    val logging     = "com.codahale"              %% "logula"              % "2.1.3"

  }

}

