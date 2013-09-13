package actors

import play.api.libs.json._
import play.api.libs.json.Json._

import akka.actor.Actor

import play.api.libs.iteratee.{Concurrent, Enumerator}

import play.api.libs.iteratee.Concurrent.Channel
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._

/**
 * User: Luigi Antonini
 * Date: 19/07/13
 * Time: 15.38
 */
class TasksActor extends Actor {

  val cancellable = context.system.scheduler.schedule(0 second, 1 second, self, UpdateTime())

  case class UserChannel(userId: Int, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getName)

  var webSockets = Map[Int, UserChannel]()
  var usersTasks = Map[Int, Int]()

  override def receive = {

    case StartSocket(userId) =>

      log.debug(s"start new socket for user $userId")

      val userChannel: UserChannel = webSockets.get(userId) getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(userId, 0, broadcast._1, broadcast._2)
      }

      userChannel.channelsCount = userChannel.channelsCount + 1
      webSockets += (userId -> userChannel)

      log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      log debug s"channel count : ${webSockets.size}"

      sender ! userChannel.enumerator

    case UpdateTime() =>

      usersTasks.foreach {
        case (userId, millis) =>
          usersTasks += (userId -> (millis + 1000))

          val json = Map("data" -> toJson(millis))
          webSockets.get(userId).get.channel push Json.toJson(json)
      }


    case Start(userId) =>
      usersTasks += (userId -> 0)

    case Stop(userId) =>
      removeUserTimer(userId)
      
      val json = Map("data" -> toJson(0))
      webSockets.get(userId).get.channel push Json.toJson(json)

    case SocketClosed(userId) =>

      log debug s"closed socket for $userId"

      val userChannel = webSockets.get(userId).get

      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        webSockets += (userId -> userChannel)
        log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(userId)
        removeUserTimer(userId)
        log debug s"removed channel and timer for $userId"
      }

  }

  def removeUserTimer(userId: Int) = usersTasks -= userId
  def removeUserChannel(userId: Int) = webSockets -= userId

}


sealed trait SocketMessage

case class StartSocket(userId: Int) extends SocketMessage

case class SocketClosed(userId: Int) extends SocketMessage

case class UpdateTime() extends SocketMessage

case class Start(userId: Int) extends SocketMessage

case class Stop(userId: Int) extends SocketMessage

