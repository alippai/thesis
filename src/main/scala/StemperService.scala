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

  val file_1k = scala.io.Source.fromURL(getClass.getClassLoader.getResource("templates/1k.html"))("UTF-8").mkString
  val file_2k = scala.io.Source.fromURL(getClass.getClassLoader.getResource("templates/2k.html"))("UTF-8").mkString
  val file_5k = scala.io.Source.fromURL(getClass.getClassLoader.getResource("templates/5k.html"))("UTF-8").mkString
  val file_10k = scala.io.Source.fromURL(getClass.getClassLoader.getResource("templates/10k.html"))("UTF-8").mkString
  val t_1k = new Mustache(file_1k)
  val t_2k = new Mustache(file_2k)
  val t_5k = new Mustache(file_5k)
  val t_10k = new Mustache(file_10k)

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self, fastPath = fastPath)

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/1k"), _, _, _) =>
      sender ! HttpResponse(entity = HttpEntity(ContentType(`text/html`, `UTF-8`), t_1k.render(Map("name"->"world", "test" -> System.currentTimeMillis.toString()))))
    case HttpRequest(GET, Uri.Path("/2k"), _, _, _) =>
      sender ! HttpResponse(entity = HttpEntity(ContentType(`text/html`, `UTF-8`), t_2k.render(Map("name"->"world", "test" -> System.currentTimeMillis.toString()))))
    case HttpRequest(GET, Uri.Path("/5k"), _, _, _) =>
      sender ! HttpResponse(entity = HttpEntity(ContentType(`text/html`, `UTF-8`), t_5k.render(Map("name"->"world", "test" -> System.currentTimeMillis.toString()))))
    case HttpRequest(GET, Uri.Path("/10k"), _, _, _) =>
      sender ! HttpResponse(entity = HttpEntity(ContentType(`text/html`, `UTF-8`), t_10k.render(Map("name"->"world", "test" -> System.currentTimeMillis.toString()))))

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

  def index() = HttpResponse(
    entity = HttpEntity(ContentType(`text/html`, `UTF-8`), file_1k)
  )
}