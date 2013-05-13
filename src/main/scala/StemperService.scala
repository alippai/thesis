package stemper

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import HttpCharsets._
import java.net.URL

class StemperService extends Actor with SprayActorLogging {
  implicit val timeout: Timeout = 2.second // for the actor 'asks'
  import context.dispatcher // ExecutionContext for the futures and scheduler
  import Uri._
  import Uri.Path._

  def fastPath: Http.FastPath = {
    case HttpRequest(GET, Uri(_, _, Slash(Segment("fast-ping", Path.Empty)), _, _), _, _, _) =>
      HttpResponse(entity = "FAST-PONG!")
  }

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    //case _: Http.Connected => sender ! Http.Register(self, fastPath = fastPath)

    case _: Http.Connected => sender ! Http.Register(self, fastPath = fastPath)

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case _: HttpRequest =>
      sender ! index()

    case Timedout(HttpRequest(_, Uri.Path("/timeout/timeout"), _, _, _)) =>
      log.info("Dropping Timeout message")

    case Timedout(HttpRequest(method, uri, _, _, _)) =>
      sender ! HttpResponse(
        status = 500,
        entity = "The " + method + " request to '" + uri + "' has timed out..."
      )
  }

  //val source = scala.io.Source.fromURL(new URL("http://ustream.tv/")).mkString

  val myURL = getClass.getClassLoader.getResource("file.html")
  val source = scala.io.Source.fromURL(myURL)("UTF-8").mkString
  val template = new Mustache(source)

  def index() = HttpResponse(
    entity = HttpEntity(ContentType(`text/html`, `UTF-8`), template.render(Map("name"->"world", "test" -> System.currentTimeMillis.toString())))
    //entity = HttpEntity(`text/html`, template.render(Map("name"->"world")))
  )
}