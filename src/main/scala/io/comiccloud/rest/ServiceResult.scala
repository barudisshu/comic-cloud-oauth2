package io.comiccloud.rest

sealed abstract class ServiceResult[+A] {
  def isEmpty: Boolean
  def isValid: Boolean
  def getOrElse[B >: A](default: => B): B                    = default
  def map[B](f: A => B): ServiceResult[B]                    = EmptyResult
  def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = EmptyResult
  def filter(p: A => Boolean): ServiceResult[A]              = this
  def toOption: Option[A] = this match {
    case FullResult(a) => Some(a)
    case _             => None
  }
}

object ServiceResult {
  val UnexpectedFailure =
    ErrorMessage("common.unexpect", Some("An unexpected exception has occurred"))

  def fromOption[A](opt: Option[A]): ServiceResult[A] = opt match {
    case None        => EmptyResult
    case Some(value) => FullResult(value)
  }
}

sealed abstract class Empty extends ServiceResult[Nothing] {
  def isValid: Boolean = false
  def isEmpty: Boolean = true
}
case object EmptyResult extends Empty

final case class FullResult[+A](value: A) extends ServiceResult[A] {
  def isValid: Boolean                                                = true
  def isEmpty: Boolean                                                = false
  override def getOrElse[B >: A](default: => B): B                    = value
  override def map[B](f: A => B): ServiceResult[B]                    = FullResult(f(value))
  override def filter(p: A => Boolean): ServiceResult[A]              = if (p(value)) this else EmptyResult
  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = f(value)
}

object FailureType extends Enumeration {
  val Validation, Service, Authorization = Value
}

case class ErrorMessage(code: String, shortText: Option[String] = None, params: Option[Map[String, String]] = None)

object ErrorMessage {
  val InvalidEntityId = ErrorMessage("invalid.entity.id", Some("No matching entity found"))
  val InvalidAuthOp   = ErrorMessage("invalid.auth.op", Some("Authorization fail"))
}

sealed case class Failure(failType: FailureType.Value, message: ErrorMessage, exception: Option[Throwable] = None)
    extends Empty {
  type A = Nothing
  override def map[B](f: A => B): ServiceResult[B]                    = this
  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = this
}
