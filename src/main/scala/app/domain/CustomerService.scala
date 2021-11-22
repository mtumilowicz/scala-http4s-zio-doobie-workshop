package app.domain

import zio.{IO, Task, URLayer, ZIO}

import java.io.IOException

case class CustomerService(idService: IdService, repository: CustomerRepository) {

  def getAll: IO[IOException, List[Customer]] =
     repository.getAll

  def getById(id: CustomerId): IO[IOException, Option[Customer]] =
    repository.getById(id)

  def delete(id: CustomerId): IO[IOException, Unit] =
    repository.delete(id)

  def deleteAll: IO[IOException, Unit] =
    repository.deleteAll

  def create(command: NewCustomerCommand): Task[Customer] = {
    for {
      id <- idService.generate()
      customer = command.toCustomer(CustomerId(id))
      created <- repository.create(customer)
    } yield created
  }

}

object CustomerService {

  def create(command: NewCustomerCommand): ZIO[CustomerServiceEnv, Throwable, Customer] = ZIO.accessM(_.get.create(command))

  def getById(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Option[Customer]] = ZIO.accessM(_.get.getById(id))

  val getAll: ZIO[CustomerServiceEnv, IOException, List[Customer]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Unit] = ZIO.accessM(_.get.delete(id))

  val deleteAll: ZIO[CustomerServiceEnv, IOException, Unit] =
    ZIO.accessM(_.get.deleteAll)

  val live: URLayer[CustomerRepositoryEnv with IdServiceEnv, CustomerServiceEnv] = {
    for {
      service <- ZIO.service[IdService]
      repository <- ZIO.service[CustomerRepository]
    } yield CustomerService(service, repository)
  }.toLayer
}
