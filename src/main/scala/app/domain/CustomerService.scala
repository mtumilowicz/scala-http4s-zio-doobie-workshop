package app.domain

import zio.{IO, URLayer, ZIO}

import java.io.IOException
import zio.console.Console

case class CustomerService(console: Console.Service, repository: CustomerRepository) {

  def getAll: IO[IOException, List[Customer]] =
    console.putStrLn("Business method") *> repository.getAll

  def getById(id: CustomerId): IO[IOException, Option[Customer]] =
    console.putStrLn("Business method") *> repository.getById(id)

  def delete(id: CustomerId): IO[IOException, Unit] =
    console.putStrLn("Business method") *> repository.delete(id)

  def deleteAll: IO[IOException, Unit] =
    console.putStrLn("Business method") *> repository.deleteAll

  def create(command: NewCustomerCommand): IO[IOException, Customer] =
    console.putStrLn("Business method") *> repository.create(Customer.createFrom(command))

}

object CustomerService {

  def create(command: NewCustomerCommand): ZIO[CustomerServiceEnv, IOException, Customer] = ZIO.accessM(_.get.create(command))

  def getById(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Option[Customer]] = ZIO.accessM(_.get.getById(id))

  val getAll: ZIO[CustomerServiceEnv, IOException, List[Customer]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: CustomerId): ZIO[CustomerServiceEnv, IOException, Unit] = ZIO.accessM(_.get.delete(id))

  val deleteAll: ZIO[CustomerServiceEnv, IOException, Unit] =
    ZIO.accessM(_.get.deleteAll)

  val live: URLayer[CustomerRepositoryEnv with Console, CustomerServiceEnv] = {
    for {
      console <- ZIO.service[Console.Service]
      repository <- ZIO.service[CustomerRepository]
    } yield new CustomerService(console, repository)
  }.toLayer
}
