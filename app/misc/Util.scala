package misc

import java.sql.{Timestamp, Time, Date}
import java.util.Calendar

/**
 * Created with IntelliJ IDEA.
 * User: luigi
 * Date: 19/04/13
 * Time: 19:16
 * To change this template use File | Settings | File Templates.
 */
object Util {

  def singleton[T](name: String)(implicit man: Manifest[T]): T =
    Class.forName(name + "$").getField("MODULE$").get(man.runtimeClass).asInstanceOf[T]

  def sqlDate = new Date(timeMillis)
  def sqlTime(time: Long) = new Time(time)
  def sqlTimestamp = new Timestamp(timeMillis)
  def sqlTimestamp(time: Long) = new Timestamp(time)

  def timeMillis = Calendar.getInstance().getTimeInMillis

  def hours(time: Long) = addZero(time/(1000*60*60))
  def minutes(time: Long) = addZero((time/(1000*60))%60)
  def seconds(time: Long) = addZero((time/1000)%60)

  def addZero(value: Long) = 
     if(value < 10)
        s"0${value}"
      else
        value

  def formatTime(time: Long) = s"${hours(time)}:${minutes(time)}:${seconds(time)}"

}
