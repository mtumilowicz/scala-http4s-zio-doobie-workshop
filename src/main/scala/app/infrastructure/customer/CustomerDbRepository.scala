package app.infrastructure.customer

import app.domain.customer.{Customer, CustomerId, CustomerRepository, CustomerRepositoryEnv}
import app.infrastructure.config._
import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._

final private class CustomerDbRepository(xa: Transactor[Task]) extends CustomerRepository {

  import CustomerDbRepository.SQL

  override def getAll: fs2.Stream[Task, Customer] =
    SQL.getAll
      .stream
      .transact(xa)

  override def getById(id: CustomerId): UIO[Option[Customer]] =
    SQL
      .get(id)
      .option
      .transact(xa)
      .orDie

  override def delete(id: CustomerId): UIO[Option[CustomerId]] =
    SQL
      .delete(id)
      .run
      .transact(xa)
      .orDie
      .map(rowsAffected => if (rowsAffected == 0) None else Some(id))

  override def deleteAll(): UIO[Unit] =
    SQL.deleteAll.run
      .transact(xa)
      .unit
      .orDie

  override def create(customer: Customer): UIO[Customer] =
    SQL
      .create(customer)
      .withUniqueGeneratedKeys[String]("ID")
      .map(_ => customer)
      .transact(xa)
      .orDie
}

object CustomerDbRepository {

  def live: ZLayer[DoobieTransactorConfigEnv, Throwable, CustomerRepositoryEnv] = {
    ZLayer.fromManaged {
      for {
        transactor <- getTransactor.toManaged_
      } yield new CustomerDbRepository(transactor)
    }
  }

  object SQL {
    val getAll: Query0[Customer] = sql"""
      SELECT * FROM Customers
      """.query[Customer]

    val deleteAll: Update0 =
      sql"""
      DELETE from Customers
      """.update

    def create(customer: Customer): Update0 =
      sql"""
      INSERT INTO Customers (ID, NAME, LOCKED)
      VALUES (${customer.id}, ${customer.name}, ${customer.locked})
      """.update

    def get(id: CustomerId): Query0[Customer] = sql"""
      SELECT * FROM Customers WHERE ID = ${id.value}
      """.query[Customer]

    def delete(id: CustomerId): Update0 =
      sql"""
      DELETE from Customers WHERE ID = ${id.value}
      """.update

  }
}
