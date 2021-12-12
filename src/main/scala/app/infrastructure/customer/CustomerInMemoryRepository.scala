package app.infrastructure.customer

import app.domain.customer._
import zio._

final private class CustomerInMemoryRepository(ref: Ref[Map[CustomerId, Customer]])
  extends CustomerRepository {

  override def getAll: fs2.Stream[Task, Customer] =
    fs2.Stream.eval(ref.get.map(_.values.toList))
      .flatMap(fs2.Stream.emits(_))

  override def delete(id: CustomerId): UIO[Option[CustomerId]] =
    getById(id)
      .flatMap(_ => ref.update(store => store - id))
      .map(_ => Some(id))

  override def getById(id: CustomerId): UIO[Option[Customer]] =
    ref.get.map(_.get(id))

  override def deleteAll: UIO[Unit] =
    ref.update(_.empty)

  override def create(customer: Customer): UIO[Customer] =
    for {
      _ <- ref.update(store => store + (customer.id -> customer))
    } yield customer
}

object CustomerInMemoryRepository {

  val live: URLayer[Any, CustomerRepositoryEnv] =
    ZLayer.fromEffect {
      for {
        ref <- Ref.make(Map.empty[CustomerId, Customer])
      } yield new CustomerInMemoryRepository(ref)
    }
}
