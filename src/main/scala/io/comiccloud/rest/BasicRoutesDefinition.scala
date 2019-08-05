package io.comiccloud.rest

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import akka.stream.Materializer
import akka.util.Timeout
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

object BasicRoutesDefinition {
  val NotFoundResp: ApiResponse[String] =
    ApiResponse[String](ApiResponseMeta(NotFound.intValue, Some(ErrorMessage("notfound"))))
  val UnexpectedFailResp: ApiResponse[String] =
    ApiResponse[String](ApiResponseMeta(InternalServerError.intValue, Some(ServiceResult.UnexpectedFailure)))
}

/**
 * Trait that represents a place where a set of routes for the bookstore app are constructed
 */
trait BasicRoutesDefinition extends ApiResponseJsonProtocol {

  import BasicRoutesDefinition._

  import concurrent.duration._

  implicit val endpointTimeout: Timeout = Timeout(10 seconds)
  def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route

  def service[T: ClassTag](msg: Any, ref: ActorRef): Future[ServiceResult[T]] = {
    import akka.pattern.ask
    (ref ? msg).mapTo[ServiceResult[T]]
  }

  def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
    val fut = service[T](msg, ref)
    onComplete(fut) {
      case util.Success(FullResult(t)) =>
        val resp = ApiResponse(ApiResponseMeta(OK.intValue), Some(t))
        complete(resp)

      case util.Success(EmptyResult) =>
        complete((NotFound, NotFoundResp))

      case util.Success(Failure(FailureType.Validation, ErrorMessage.InvalidEntityId, _)) =>
        complete((NotFound, NotFoundResp))

      case util.Success(Failure(FailureType.Authorization, ErrorMessage.InvalidAuthOp, _)) =>
        reject(AuthorizationFailedRejection)

      case util.Success(fail: Failure) =>
        val status = fail.failType match {
          case FailureType.Validation => BadRequest
          case FailureType.Authorization => Unauthorized
          case _                      => InternalServerError
        }
        val apiResp = ApiResponse[String](ApiResponseMeta(status.intValue, Some(fail.message)))
        complete((status, apiResp))

      case util.Failure(ex) =>
        complete((InternalServerError, UnexpectedFailResp))
    }
  }
}