package io.comiccloud.rest

import spray.json._

/**
 * Representation of a response from a REST api call.  Contains meta data as well as the optional
 * response payload if there was no error
 */
case class ApiResponse[T](meta: ApiResponseMeta, response: Option[T] = None)

/**
 * Meta data about the response that will contain status code and any error info if there was an error
 */
case class ApiResponseMeta(statusCode: Int, error: Option[ErrorMessage] = None)

/**
 * Json protocol class for the api response set of types
 */
trait ApiResponseJsonProtocol extends ServiceProtocol {
  implicit val errorMessageFormat: RootJsonFormat[ErrorMessage] = jsonFormat3(ErrorMessage.apply)
  implicit val metaFormat: RootJsonFormat[ApiResponseMeta] = jsonFormat2(ApiResponseMeta)
  implicit def apiRespFormat[T: JsonFormat]: RootJsonFormat[ApiResponse[T]] = jsonFormat2(ApiResponse.apply[T])
}
