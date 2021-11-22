package app.infrastructure.customer

import app.domain.{Customer, CustomerId, CustomerRepository, CustomerRepositoryEnv}
import zio._

final private class CustomerInMemoryRepository(ref: Ref[Map[CustomerId, Customer]])
  extends CustomerRepository {

  override def getAll: UIO[List[Customer]] = ref.get.map(_.values.toList)

  override def getById(id: CustomerId): UIO[Option[Customer]] = ref.get.map(_.get(id))

  override def delete(id: CustomerId): UIO[Unit] = ref.update(store => store - id).unit

  override def deleteAll: UIO[Unit] = ref.update(_.empty).unit

  override def create(customer: Customer): UIO[Customer] =
    for {
      _     <- ref.update(store => store + (customer.id -> customer))
    } yield customer
}
object CustomerInMemoryRepository {

  val live: URLayer[Any, CustomerRepositoryEnv] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[CustomerId, Customer])
      } yield new CustomerInMemoryRepository(ref)
    }
}
