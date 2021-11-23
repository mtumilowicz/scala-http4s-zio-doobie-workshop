package app.domain

import zio.{IO, Task}

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