package app.infrastructure.customer

import app.domain.CustomerRepository
import cats.effect.Blocker
import app.domain._
import app.infrastructure.config._
import doobie._
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import java.util.UUID

final private class CustomerDbRepository(xa: Transactor[Task]) extends CustomerRepository {
  import CustomerDbRepository.SQL

  override def getAll: UIO[List[Customer]] =
    SQL.getAll
      .to[List]
      .transact(xa)
      .orDie

  override def getById(id: CustomerId): UIO[Option[Customer]] =
    SQL
      .get(id)
      .option
      .transact(xa)
      .orDie

  override def delete(id: CustomerId): UIO[Unit] =
    SQL
      .delete(id)
      .run
      .transact(xa)
      .unit
      .orDie

  override def deleteAll: UIO[Unit] =
    SQL.deleteAll.run
      .transact(xa)
      .unit
      .orDie

  override def create(command: NewCustomerCommand): UIO[Customer] =
    SQL
      .create(command)
      .withUniqueGeneratedKeys[String]("ID")
      .map(id => command.toCustomer(CustomerId(id)))
      .transact(xa)
      .orDie
}

object CustomerDbRepository {

  def live: ZLayer[Blocking with DatabaseConfigEnv, Throwable, CustomerRepositoryEnv] = {
    def initDb(cfg: DatabaseConfig): Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(
      cfg: DatabaseConfig
    ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- Managed.succeed(
                          rt.environment
                            .get[Blocking.Service]
                            .blockingExecutor
                            .asEC
                        )
          connectEC   = rt.platform.executor.asEC
          transactor <- HikariTransactor
                          .newHikariTransactor[Task](
                            cfg.driver,
                            cfg.url,
                            cfg.user,
                            cfg.password,
                            connectEC,
                            Blocker.liftExecutionContext(transactEC)
                          )
                          .toManaged
        } yield transactor
      }

    ZLayer.fromManaged {
      for {
        cfg        <- getDatabaseConfig.toManaged_
        _          <- initDb(cfg).toManaged_
        transactor <- mkTransactor(cfg)
      } yield new CustomerDbRepository(transactor)
    }
  }

  object SQL {
    def create(command: NewCustomerCommand): Update0 = sql"""
      INSERT INTO Customers (ID, NAME, LOCKED)
      VALUES (${UUID.randomUUID().toString}, ${command.name}, ${command.locked})
      """.update

    def get(id: CustomerId): Query0[Customer] = sql"""
      SELECT * FROM Customers WHERE ID = ${id.value}
      """.query[Customer]

    val getAll: Query0[Customer] = sql"""
      SELECT * FROM Customers
      """.query[Customer]

    def delete(id: CustomerId): Update0 = sql"""
      DELETE from Customers WHERE ID = ${id.value}
      """.update

    val deleteAll: Update0 = sql"""
      DELETE from Customers
      """.update

  }
}
