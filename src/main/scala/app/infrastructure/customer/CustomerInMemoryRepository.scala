package app.infrastructure.customer

import app.domain.{Customer, CustomerId, CustomerRepository, CustomerRepositoryEnv, NewCustomerCommand}
import zio._

final private class CustomerInMemoryRepository(ref: Ref[Map[CustomerId, Customer]], counter: Ref[Int])
  extends CustomerRepository {

  override def getAll: UIO[List[Customer]] = ref.get.map(_.values.toList)

  override def getById(id: CustomerId): UIO[Option[Customer]] = ref.get.map(_.get(id))

  override def delete(id: CustomerId): UIO[Unit] = ref.update(store => store - id).unit

  override def deleteAll: UIO[Unit] = ref.update(_.empty).unit

  override def create(command: NewCustomerCommand): UIO[Customer] =
    for {
      newId <- counter.updateAndGet(_ + 1).map(_.toString).map(CustomerId)
      customer   = command.toCustomer(newId)
      _     <- ref.update(store => store + (newId -> customer))
    } yield customer
}
object CustomerInMemoryRepository {

  val live: URLayer[Any, CustomerRepositoryEnv] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[CustomerId, Customer])
        counter <- Ref.make(0)
      } yield new CustomerInMemoryRepository(ref, counter)
    }
}
