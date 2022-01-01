package app.gateway

import app.domain.customer._
import app.gateway.HttpTestUtils.{checkBody, _}
import app.gateway.customer.CustomerController
import app.gateway.customer.out.CustomerApiOutput
import app.infrastructure.config.DependencyConfig
import cats.data.Kleisli
import io.circe.Decoder
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.literal._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe._
import org.http4s.implicits._
import zio._
//import zio.console.putStrLn
import zio.interop.catz._
import zio.test._

object CustomerControllerSpec extends DefaultRunnableSpec {

  type CustomerTask[A] = RIO[CustomerServiceEnv, A]

  implicit val app: Kleisli[CustomerTask, Request[CustomerTask], Response[CustomerTask]] =
    new CustomerController[CustomerServiceEnv]().routes("").orNotFound

  private val customerControllerSuite = suite("CustomerController")(
    createNewCustomerResponseTest,
    createNewCustomerThenQueryTest,
    queryNonExistingCustomerTest,
    createCustomersThenGetAllTest,
    createCustomerThenDeleteResponseTest,
    deleteNonExistingCustomerResponseTest,
    deleteAllThenQueryTest
  )

  override def spec =
    customerControllerSuite
      .provideSomeLayer[ZEnv](DependencyConfig.inMemory.appLayer)

  lazy val createNewCustomerResponseTest =
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
    }

  lazy val createNewCustomerThenQueryTest =
    testM("create new customer then query it") {
      val expectedResponse = Some(
        json"""{
            "id": "1",
            "url": "/1",
            "name": "Test",
            "locked":false
          }""")

      for {
        createdResponse <- CustomerLifecycle.create(json"""{"name": "Test"}""")
        customerId <- asApiOutput(createdResponse).map(_.id)
        response <- CustomerLifecycle.getById(customerId)
        bodyCheckResult <- checkBody(response, expectedResponse)
      } yield checkStatus(response, Status.Ok) && bodyCheckResult
    }

  lazy val queryNonExistingCustomerTest =
    testM("query non existing customer") {
      for {
        response <- CustomerLifecycle.getById("111")
      } yield checkStatus(response, Status.NotFound)
    }

  lazy val createCustomersThenGetAllTest =
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
    }

  lazy val createCustomerThenDeleteResponseTest =
    testM("create customer then delete it by id and verify answer") {
      val expectedResponse = Some(json"""[]""")
      for {
        response <- CustomerLifecycle.create(json"""{"name": "Test"}""")
        customer <- asApiOutput(response)
        customerId = customer.id
        _ <- CustomerLifecycle.deleteById(customerId)
        response <- CustomerLifecycle.getAll
        bodyCheckResult <- checkBody(response, expectedResponse)
      } yield checkStatus(response, Status.Ok) && bodyCheckResult
    }

  lazy val deleteNonExistingCustomerResponseTest =
    testM("should return not found when deleting non existing customer") {
      val expectedResponse = Option.empty[String]

      for {
        response <- CustomerLifecycle.deleteById("1")
        bodyCheckResult <- checkBody(response, expectedResponse)
      } yield checkStatus(response, Status.NotFound) && bodyCheckResult
    }

  lazy val deleteAllThenQueryTest = testM("should delete all customers") {
    val expectedResponse = Some(json"""[]""")
    for {
      _ <- CustomerLifecycle.create(json"""{"name": "Test"}""")
      _ <- CustomerLifecycle.create(json"""{"name": "Test2"}""")
      _ <- CustomerLifecycle.deleteAll
      response <- CustomerLifecycle.getAll
      bodyCheckResult <- checkBody(response, expectedResponse)
    } yield checkStatus(response, Status.Ok) && bodyCheckResult
  }

  private def asApiOutput(response: Response[CustomerTask]): CustomerTask[CustomerApiOutput] = {
    implicit def circeJsonDecoder[A](implicit
                                     decoder: Decoder[A]
                                    ): EntityDecoder[CustomerTask, A] = jsonOf[CustomerTask, A]

    response.as[CustomerApiOutput]
  }
}
