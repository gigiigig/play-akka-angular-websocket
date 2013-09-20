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

  val timerActor = Akka.system.actorOf(Props[TimerActor])

  /**
   * This function crate a WebSocket using the
   * enumertator linked to the current user,
   * retreived from the TaskActor. 
   */
  def indexWS = withAuthWS {
    userId =>

      implicit val timeout = Timeout(3 seconds)

      // using the ask pattern of akka, 
      // get the enumerator for that user
      (timerActor ? StartSocket(userId)) map {
        enumerator =>

          // create a Itreatee which ignore the input and
          // and send a SocketClosed message to the actor when
          // connection is closed from the client
          (Iteratee.ignore[JsValue] mapDone {
            _ =>
              timerActor ! SocketClosed(userId)
          }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }
  }

  def start = withAuth {
    userId => implicit request =>
      timerActor ! Start(userId)
      Ok("")
  }


  def stop = withAuth {
    userId => implicit request =>
      timerActor ! Stop(userId)
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
   * Basi authentication system
   * try to retieve the username, call f() if it is present,
   * or unauthF() otherwise
   */
  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(username, unauthF) {
      username =>
        Action(request => f(username.toInt)(request))
    }
  }

  /**
   * This function provide a basic authentication for 
   * WebSocket, lekely withAuth function try to retrieve the
   * the username form the session, and call f() funcion if find it,
   * or create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
   * if username is none  
   */
  def withAuthWS(f: => Int => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue] = {

    // this function create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
    // the itaratee ignore the input and do nothing,
    // and the enumerator just send a 'not authorized message'
    // and close the socket, sending Enumerator.eof
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

