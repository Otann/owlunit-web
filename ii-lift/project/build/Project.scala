import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.3"
  
  /**
   * Application dependencies
   */
  val webkit    = "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default"
  val logback   = "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
  
  val servlet   = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  val jetty6    = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default"  
  val junit     = "junit" % "junit" % "4.5" % "test->default"
  val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  
  /**
   * Owls dependencies
   */
  val ii_code    = "com.manymonkeys" % "ii-core"    % "1.0" % "compile->default"
  val ii_service = "com.manymonkeys" % "ii-service" % "1.0" % "compile->default"

  /**
   * Maven repositories
   */
  lazy val scalatoolsSnapshots = ScalaToolsSnapshots
  lazy val mavenLocal : Resolver = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
}