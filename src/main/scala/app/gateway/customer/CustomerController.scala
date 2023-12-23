package app.gateway.customer

import app.gateway.customer.in.NewCustomerApiInput
import app.domain.customer._
import app.gateway.customer.out.CustomerApiOutput
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import org.http4s.HttpRoutes
import zio._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
//import zio.interop.catz.implicits._
//
//import java.util.concurrent.TimeUnit
//import scala.concurrent.duration.FiniteDuration

class CustomerController[R <: CustomerServiceEnv] {
  type CustomerTask[A] = RIO[R, A]
  type CustomerStream = fs2.Stream[CustomerTask, Customer]

  def routes(rootUri: String): HttpRoutes[RIO[R, *]] = {

    val dsl: Http4sDsl[CustomerTask] = Http4sDsl[CustomerTask]
    import dsl._

    HttpRoutes.of[CustomerTask] {
      case GET -> Root / id =>
        for {
          customer <- ZIO.serviceWith[CustomerService](_.getById(CustomerId(id)))
          response <- customer.fold(NotFound())(x => Ok(CustomerApiOutput(rootUri, x)))
        } yield response

      case GET -> Root =>
        val pipeline: RIO[CustomerServiceEnv, fs2.Stream[CustomerTask, Customer]] =
          ZIO.service[CustomerService].map(_.getAll)
//            .map(_.metered(FiniteDuration(2, TimeUnit.SECONDS)))
        for {
          stream <- pipeline
          json <- Ok(stream.map(CustomerApiOutput(rootUri, _)).map(_.asJson))
        } yield json

      case req@POST -> Root =>
        req.decode[NewCustomerApiInput] { input =>
          ZIO.serviceWith[CustomerService](_.create(input.toDomain))
            .map(CustomerApiOutput(rootUri, _))
            .flatMap(Created(_))
        }

      case DELETE -> Root / id =>
        ZIO.serviceWith[CustomerService](_.delete(CustomerId(id)))
          .flatMap(_.fold(NotFound())(_ => Ok(id)))

      case DELETE -> Root =>
        ZIO.serviceWith[CustomerService](_.deleteAll()) *> Ok()
    }
  }
}
