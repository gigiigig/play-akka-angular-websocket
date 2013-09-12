package misc

import play.api._
import play.api.mvc._
import play.api.mvc.Results._


/**
 * Created with IntelliJ IDEA.
 * User: luigi
 * Date: 18/04/13
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Redirect(controllers.routes.AppController.index())
  }

  override def onStop(app: Application) = {
  }

}
