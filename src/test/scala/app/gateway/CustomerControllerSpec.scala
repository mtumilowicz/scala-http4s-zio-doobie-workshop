package app.gateway

import app.domain.customer._
import app.gateway.HTTPSpec._
import app.gateway.customer.CustomerController
import app.gateway.customer.out.CustomerApiOutput
import app.infrastructure.config.DependencyConfig
import io.circe.Decoder
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test._

object CustomerControllerSpec extends DefaultRunnableSpec {

  type CustomerTask[A] = RIO[CustomerServiceEnv, A]

  val app = new CustomerController[CustomerServiceEnv]().routes("").orNotFound

  override def spec =
    suite("CustomerController")(
      testM("should create new customer") {
        val req = request[CustomerTask](Method.POST, "/")
          .withEntity(json"""{"name": "Test"}""")
        checkRequest(
          app.run(req),
          Status.Created,
          Some(
            json"""{
            "id": "1",
            "url": "/1",
            "name": "Test",
            "locked":false
          }""")
        )
      },
      testM("should list all customers") {
        val setupReq =
          request[CustomerTask](Method.POST, "/")
            .withEntity(json"""{"name": "Test"}""")
        val req = request[CustomerTask](Method.GET, "/")
        checkRequest(
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(
            json"""[
              {"id": "1", "url": "/1", "name": "Test", "locked":false},
              {"id": "2", "url": "/2", "name": "Test", "locked":false}
            ]""")
        )
      },
      testM("should delete customer by id") {
        val setupReq =
          request[CustomerTask](Method.POST, "/")
            .withEntity(json"""{"name": "Test"}""")
        val deleteReq =
          (id: String) => request[CustomerTask](Method.DELETE, s"/$id")
        val req = request[CustomerTask](Method.GET, "/")
        checkRequest(
          app
            .run(setupReq)
            .flatMap { resp =>
              implicit def circeJsonDecoder[A](implicit
                                               decoder: Decoder[A]
                                              ): EntityDecoder[CustomerTask, A] = jsonOf[CustomerTask, A]

              resp.as[CustomerApiOutput].map(_.id)
            }
            .flatMap(id => app.run(deleteReq(id))) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      },
      testM("should delete all customers") {
        val setupReq =
          request[CustomerTask](Method.POST, "/")
            .withEntity(json"""{"name": "Test"}""")
        val deleteReq = request[CustomerTask](Method.DELETE, "/")
        val req = request[CustomerTask](Method.GET, "/")
        checkRequest(
          app.run(setupReq) *> app.run(setupReq) *> app
            .run(deleteReq) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      }
    ).provideSomeLayer[ZEnv](DependencyConfig.inMemory.appLayer)
}
