package app.infrastructure.config.customer

import app.domain.{Customer, CustomerId, CustomerServiceEnv, NewCustomerCommand}
import zio._

object CustomerServiceProxy {

  val getAll: RIO[CustomerServiceEnv, fs2.Stream[Task, Customer]] =
    RIO.access(_.get.getAll)

  val deleteAll: URIO[CustomerServiceEnv, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def create(command: NewCustomerCommand): RIO[CustomerServiceEnv, Customer] =
    ZIO.accessM(_.get.create(command))

  def getById(id: CustomerId): URIO[CustomerServiceEnv, Option[Customer]] =
    ZIO.accessM(_.get.getById(id))

  def delete(id: CustomerId): URIO[CustomerServiceEnv, Option[CustomerId]] =
    ZIO.accessM(_.get.delete(id))

}
