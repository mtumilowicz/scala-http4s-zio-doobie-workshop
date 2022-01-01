package app.infrastructure.config.db

import app.infrastructure.config.{DatabaseConfigEnv, DoobieTransactorConfigEnv, getDatabaseConfig}
import doobie.Transactor
import zio.interop.catz._
import zio.{Task, ZLayer, ZManaged}

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
        cfg <- getDatabaseConfig.toManaged_
        transactor <- mkTransactor(cfg)
      } yield transactor
    }

}
