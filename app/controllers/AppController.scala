package controllers

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}

import scala.concurrent.Future
import scala.concurrent.duration._
import actors._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import actors.UpdateTime
import actors.StartSocket
import actors.SocketClosed
import scala.util.Random
import play.api.Routes

/**
 * User: Luigi Antonini
 * Date: 17/06/13
 * Time: 23:25
 */
object AppController extends Controller with Secured{

  def index = withAuth {
    implicit request => userId =>
      Ok(views.html.app.index())
  }

  val tasksActor = Akka.system.actorOf(Props[TasksActor])

  def indexWS = withAuthWS {
    userId =>

      implicit val timeout = Timeout(3 seconds)

      (tasksActor ? StartSocket(userId)) map {
        enumerator =>
          (Iteratee.ignore[JsValue] mapDone {
            _ =>
              tasksActor ! SocketClosed(userId)
          }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }
  }

  def start = withAuth {
    userId => implicit request =>
      tasksActor ! Start(userId)
      Ok("")
  }


  def stop = withAuth {
    userId => implicit request =>
      tasksActor ! Stop(userId)
      Ok("")
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.AppController.indexWS,
          routes.javascript.AppController.start,
          routes.javascript.AppController.stop
        )
      ).as("text/javascript")
  }

}

trait Secured {
  def username(request: RequestHeader) = {
    //verify or create session, this should be a real login
    request.session.get(Security.username) 
  }

  /**
   * When user not have a session, this function create a 
   * random userId and reload index page
   */
  def unauthF(request: RequestHeader) = {
    val newId: String = new Random().nextInt().toString()
    Redirect(routes.AppController.index).withSession(Security.username -> newId)
  }

  /**
   * Base authentication
   */
  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(username, unauthF) {
      username =>
        Action(request => f(username.toInt)(request))
    }
  }

  /**
   * Base authentication for WebSocket
   */
  def withAuthWS(f: => Int => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue] = {

    def errorFuture = {
      // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]

      // Send a single 'Hello!' message and close
      val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)

      Future {
        (in, out)
      }
    }

    WebSocket.async[JsValue] {
      request =>
        username(request) match {
          case None =>
            errorFuture

          case Some(id) =>
            f(id.toInt)
            
        }
    }
  }
}

