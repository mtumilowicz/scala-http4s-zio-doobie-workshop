package app.infrastructure.config.customer

import app.domain._
import app.infrastructure.config.DatabaseConfigEnv
import app.infrastructure.customer.{CustomerDbRepository, CustomerInMemoryRepository}
import zio.blocking.Blocking
import zio.{URLayer, ZIO, ZLayer}

object CustomerConfig {

  val inMemoryRepository: URLayer[Any, CustomerRepositoryEnv] =
    CustomerInMemoryRepository.live

  val dbRepository: ZLayer[Blocking with DatabaseConfigEnv, Throwable, CustomerRepositoryEnv] =
    CustomerDbRepository.live

  val service: URLayer[CustomerRepositoryEnv with IdServiceEnv, CustomerServiceEnv] = {
    for {
      service <- ZIO.service[IdService]
      repository <- ZIO.service[CustomerRepository]
    } yield CustomerService(service, repository)
  }.toLayer

}
