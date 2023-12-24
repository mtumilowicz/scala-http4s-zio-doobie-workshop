package app.gateway

import org.http4s._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._

object HttpTestUtils {

  def makeRequest[F[_]](
                     method: Method,
                     uri: String
                   ): Request[F] = Request(method = method, uri = Uri.fromString(uri).toOption.get)

  def checkStatus[R](
                         response: Response[RIO[R, *]],
                         expectedStatus: Status
                       ): TestResult = {
    assert(response.status)(equalTo(expectedStatus))
  }

  def checkHeader[R](response: Response[RIO[R, *]], header: Header.Raw): TestResult = {
    assert(response.headers.toList)(contains(header))
  }

  def checkBody[R, A](
                       response: Response[RIO[R, *]],
                       expectedBody: Option[A]
                        )(implicit
                          ev: EntityDecoder[RIO[R, *], A]
                        ): RIO[R, TestResult] =
    for {
      result <- expectedBody match {
        case Some(expected) => assertM(response.as[A])(equalTo(expected))
        case None => assertM(response.bodyText.compile.toVector)(isEmpty)
      }
    } yield result
}
