package app.infrastructure.db

import app.infrastructure.config.{DatabaseConfigEnv, DoobieTransactorConfigEnv}
import doobie.Transactor
import zio.interop.catz._
import zio.{Task, ZIO, ZLayer, ZManaged}

object DoobieConfig {

  def mkTransactor(
                    cfg: DatabaseConfig
                  ): ZManaged[Any, Throwable, Transactor[Task]] = {
    ZManaged.effect {
      Transactor.fromDriverManager[Task](
        cfg.driver,
        cfg.url,
        cfg.user,
        cfg.password,
      )
    }
  }

  def live: ZLayer[DatabaseConfigEnv, Throwable, DoobieTransactorConfigEnv] =
    ZLayer.fromManaged {
      for {
        cfg <- ZIO.service[DatabaseConfig].toManaged_
        transactor <- mkTransactor(cfg)
      } yield transactor
    }

}
