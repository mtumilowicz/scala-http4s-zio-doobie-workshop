package app.domain

import app.infrastructure.config.DatabaseConfigEnv
import app.infrastructure.customer.{CustomerDbRepository, CustomerInMemoryRepository}
import zio.blocking.Blocking
import zio.{UIO, URLayer, ZLayer}

trait CustomerRepository extends Serializable {

  def getAll: UIO[List[Customer]]

  def getById(id: CustomerId): UIO[Option[Customer]]

  def delete(id: CustomerId): UIO[Unit]

  def deleteAll: UIO[Unit]

  def create(customer: Customer): UIO[Customer]
}

object CustomerRepository {

  val live: ZLayer[Blocking with DatabaseConfigEnv, Throwable, CustomerRepositoryEnv] =
    CustomerDbRepository.live

  val inMemory: URLayer[Any, CustomerRepositoryEnv] =
    CustomerInMemoryRepository.live
}
