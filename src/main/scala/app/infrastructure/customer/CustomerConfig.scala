package app.infrastructure.customer

import app.domain.customer._
import app.domain.id.{IdService, IdServiceEnv}
import app.infrastructure.config.DoobieTransactorConfigEnv
import zio.{Has, URLayer, ZIO, ZLayer}

object CustomerConfig {

  val inMemoryRepository: URLayer[Any, CustomerRepositoryEnv] =
    CustomerInMemoryRepository.live

  val dbRepository: ZLayer[DoobieTransactorConfigEnv, Throwable, CustomerRepositoryEnv] =
    CustomerDbRepository.live

  val service: ZLayer[CustomerRepositoryEnv with IdServiceEnv, Nothing, Has[CustomerService]] = {
    for {
      service <- ZIO.service[IdService]
      repository <- ZIO.service[CustomerRepository]
    } yield CustomerService(service, repository)
  }.toLayer

}
