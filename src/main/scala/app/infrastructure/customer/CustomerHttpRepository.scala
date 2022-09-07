package app.infrastructure.customer

import app.domain.customer.{Customer, CustomerId, CustomerRepository, CustomerRepositoryEnv}
import app.infrastructure.config.{HttpClientConfigEnv, getHttpClient}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import zio._
import zio.interop.catz._
//import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._


final private class CustomerHttpRepository(client: Client[Task]) extends CustomerRepository with Http4sClientDsl[Task] {
  override def getAll: fs2.Stream[Task, Customer] = ???

  override def getById(id: CustomerId): UIO[Option[Customer]] =
    client.expect[String](uri"https://httpbin.org/get")
      .map(str => Customer(id, str, false))
      .map(Some(_))
      .catchAll(_ => Task.succeed(Option.empty[Customer]))

  override def delete(id: CustomerId): UIO[Option[CustomerId]] = ???

  override def deleteAll: UIO[Unit] = ???

  override def create(customer: Customer): UIO[Customer] = ???
}

object CustomerHttpRepository {
  def live: ZLayer[HttpClientConfigEnv, Throwable, CustomerRepositoryEnv] = {
    ZLayer.fromManaged {
      for {
        httpClient <- getHttpClient.toManaged_
      } yield new CustomerHttpRepository(httpClient)
    }
  }
}
