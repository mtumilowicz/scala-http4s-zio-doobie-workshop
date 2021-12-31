package app.gateway

import app.domain.customer._
import app.gateway.HttpTestUtils._
import app.gateway.customer.CustomerController
import app.gateway.customer.out.CustomerApiOutput
import app.infrastructure.config.DependencyConfig
import cats.data.Kleisli
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

  implicit val app: Kleisli[CustomerTask, Request[CustomerTask], Response[CustomerTask]] =
    new CustomerController[CustomerServiceEnv]().routes("").orNotFound

  override def spec =
    suite("CustomerController")(
      testM("create new customer and verify answer") {
        val expectedResponse = Some(
          json"""{
            "id": "1",
            "url": "/1",
            "name": "Test",
            "locked":false
          }""")

        for {
          response <- CustomerLifecycle.create(json"""{"name": "Test"}""")
          bodyCheckResult <- checkBody(response, expectedResponse)
        } yield checkStatus(response, Status.Created) && bodyCheckResult
      },
      testM("create two customers and then get all customers") {
        val expectedResponse = Some(
          json"""[
              {"id": "1", "url": "/1", "name": "Test", "locked":false},
              {"id": "2", "url": "/2", "name": "Test2", "locked":false}
            ]"""
        )

        for {
          _ <- CustomerLifecycle.create(json"""{"name": "Test"}""")
          _ <- CustomerLifecycle.create(json"""{"name": "Test2"}""")
          response <- CustomerLifecycle.getAll
          bodyCheckResult <- checkBody(response, expectedResponse)
        } yield checkStatus(response, Status.Ok) && bodyCheckResult
      },
      testM("create customer then delete it by id and verify answer") {
        val expectedResponse = Some(json"""[]""")
        for {
          customerId <- CustomerLifecycle.create(json"""{"name": "Test"}""")
            .flatMap { resp =>
              implicit def circeJsonDecoder[A](implicit
                                               decoder: Decoder[A]
                                              ): EntityDecoder[CustomerTask, A] = jsonOf[CustomerTask, A]

              resp.as[CustomerApiOutput].map(_.id)
            }
          _ <- CustomerLifecycle.deleteById(customerId)
          response <- CustomerLifecycle.getAll
          bodyCheckResult <- checkBody(response, expectedResponse)
        } yield checkStatus(response, Status.Ok) && bodyCheckResult
      },
      testM("should return not found when deleting non existing customer") {
        val expectedResponse = Option.empty[String]

        for {
          response <- CustomerLifecycle.deleteById("1")
          bodyCheckResult <- checkBody(response, expectedResponse)
        } yield checkStatus(response, Status.NotFound) && bodyCheckResult
      },
      testM("should delete all customers") {
        val expectedResponse = Some(json"""[]""")
        for {
          _ <- CustomerLifecycle.create(json"""{"name": "Test"}""")
          _ <- CustomerLifecycle.create(json"""{"name": "Test2"}""")
          _ <- CustomerLifecycle.deleteAll
          response <- CustomerLifecycle.getAll
          bodyCheckResult <- checkBody(response, expectedResponse)
        } yield checkStatus(response, Status.Ok) && bodyCheckResult
      }
    ).provideSomeLayer[ZTestEnv](DependencyConfig.inMemory.appLayer)
}
