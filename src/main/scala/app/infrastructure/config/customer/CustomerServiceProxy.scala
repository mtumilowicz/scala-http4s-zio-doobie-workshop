package app.infrastructure.config.customer

import app.domain.{Customer, CustomerId, CustomerServiceEnv, NewCustomerCommand}
import zio.ZIO

import java.io.IOException

object CustomerServiceProxy {

  val getAll: ZIO[CustomerServiceEnv, IOException, List[Customer]] =
    ZIO.accessM(_.get.getAll)
  val deleteAll: ZIO[CustomerServiceEnv, IOException, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def create(command: NewCustomerCommand): ZIO[CustomerServiceEnv, Throwable, Customer] = ZIO.accessM(_.get.create(command))

  def getById(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Option[Customer]] = ZIO.accessM(_.get.getById(id))

  def delete(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Unit] = ZIO.accessM(_.get.delete(id))

}
