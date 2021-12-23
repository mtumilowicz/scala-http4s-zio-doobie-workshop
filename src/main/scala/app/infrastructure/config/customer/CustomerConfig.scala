package app.infrastructure.config.customer

import app.domain.{ApiRepositoryEnv, InternalServiceEnv}
import app.domain.customer._
import app.domain.id.IdService
import app.infrastructure.config.DatabaseConfigEnv
import app.infrastructure.customer.{CustomerDbRepository, CustomerInMemoryRepository}
import zio.blocking.Blocking
import zio.{URLayer, ZIO, ZLayer}

object CustomerConfig {

  val inMemoryRepository: URLayer[Any, CustomerRepositoryEnv] =
    CustomerInMemoryRepository.live

  val dbRepository: ZLayer[Blocking with DatabaseConfigEnv, Throwable, CustomerRepositoryEnv] =
    CustomerDbRepository.live

  val service: URLayer[InternalServiceEnv with ApiRepositoryEnv, CustomerServiceEnv] = {
    for {
      service <- ZIO.service[IdService]
      repository <- ZIO.service[CustomerRepository]
    } yield CustomerService(service, repository)
  }.toLayer

}
