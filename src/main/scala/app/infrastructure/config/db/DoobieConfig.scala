package app.infrastructure.config.db

import app.infrastructure.config.{DatabaseConfigEnv, getDatabaseConfig}
import cats.effect.Blocker
import doobie.Transactor
import doobie.hikari.HikariTransactor
import zio.blocking.{Blocking, blocking}
import zio.interop.catz._
import zio.{Has, Task, ZIO, ZLayer, ZManaged}

object DoobieConfig {

  def mkTransactor(
                    cfg: DatabaseConfig
                  ): ZManaged[Blocking, Throwable, Transactor[Task]] =
    for {
      connectEC <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockingEC <- blocking {
        ZIO.descriptor.map(_.executor.asEC)
      }.toManaged_
      transactor <- HikariTransactor
        .newHikariTransactor[Task](
          cfg.driver,
          cfg.url,
          cfg.user,
          cfg.password,
          connectEC,
          Blocker.liftExecutionContext(blockingEC)
        )
        .toManagedZIO
    } yield transactor

  def live: ZLayer[Blocking with DatabaseConfigEnv, Throwable, Has[Transactor[Task]]] =
    ZLayer.fromManaged {
      for {
        cfg <- getDatabaseConfig.toManaged_
        transactor <- mkTransactor(cfg)
      } yield transactor
    }

}
