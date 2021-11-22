package app.gateway

import app.domain.{CustomerId, CustomerService, CustomerServiceEnv}
import app.gateway.customer.in.NewCustomerApiInput
import app.gateway.customer.out.CustomerApiOutput
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
      case GET -> Root / id =>
        for {
          customer     <- CustomerService.getById(CustomerId(id))
          response <- customer.fold(NotFound())(x => Ok(CustomerApiOutput(rootUri, x)))
        } yield response

      case GET -> Root =>
        Ok(CustomerService.getAll.map(_.map(CustomerApiOutput(rootUri, _))))

      case req @ POST -> Root =>
        req.decode[NewCustomerApiInput] { input =>
          CustomerService
            .create(input.toDomain)
            .map(CustomerApiOutput(rootUri, _))
            .flatMap(Created(_))
        }

      case DELETE -> Root / id =>
        for {
          item   <- CustomerService.getById(CustomerId(id))
          result <- item
                      .map(x => CustomerService.delete(x.id))
                      .fold(NotFound())(_.flatMap(Ok(_)))
        } yield result

      case DELETE -> Root =>
        CustomerService.deleteAll *> Ok()
    }
  }
}
