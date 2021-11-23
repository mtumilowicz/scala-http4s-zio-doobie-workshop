package app.gateway

import app.domain.{CustomerId, CustomerServiceEnv}
import app.gateway.customer.in.NewCustomerApiInput
import app.gateway.customer.out.CustomerApiOutput
import app.infrastructure.config.customer.CustomerServiceProxy
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._

object CustomerController {

  def routes[R <: CustomerServiceEnv](rootUri: String): HttpRoutes[RIO[R, *]] = {
    type CustomerTask[A] = RIO[R, A]

    val dsl: Http4sDsl[CustomerTask] = Http4sDsl[CustomerTask]
    import dsl._

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[CustomerTask, A] = jsonOf[CustomerTask, A]

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[CustomerTask, A] = jsonEncoderOf[CustomerTask, A]

    HttpRoutes.of[CustomerTask] {
      case GET -> Root / UUIDVar(id) =>
        for {
          customer <- CustomerServiceProxy.getById(CustomerId(id.toString))
          response <- customer.fold(NotFound())(x => Ok(CustomerApiOutput(rootUri, x)))
        } yield response

      case GET -> Root =>
        Ok(CustomerServiceProxy.getAll.map(_.map(CustomerApiOutput(rootUri, _))))

      case req@POST -> Root =>
        req.decode[NewCustomerApiInput] { input =>
          CustomerServiceProxy
            .create(input.toDomain)
            .map(CustomerApiOutput(rootUri, _))
            .flatMap(Created(_))
        }

      case DELETE -> Root / id =>
        CustomerServiceProxy.delete(CustomerId(id))
          .flatMap(_.fold(NotFound())(_ => Ok(id)))

      case DELETE -> Root =>
        CustomerServiceProxy.deleteAll *> Ok()
    }
  }
}
