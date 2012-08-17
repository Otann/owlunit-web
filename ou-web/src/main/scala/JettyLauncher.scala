/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.http.LiftFilter

object JettyLauncher extends App {

  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(port)
  server.setConnectors(Array(scc))

  val context = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS)
  context.setSessionHandler(new SessionHandler())
  context.addServlet(classOf[DefaultServlet], "/")
  context.addFilter(classOf[LiftFilter], "/*", 0)
  context.setResourceBase("ii-web/src/main/webapp")

  server.start
  server.join

}