package app.infrastructure

import app.infrastructure.config.db.DatabaseConfig
import app.infrastructure.config.http.HttpConfig
import zio._

package object config {
  type AppConfigEnv = Has[AppConfig]
  type HttpConfigEnv = Has[HttpConfig]
  type DatabaseConfigEnv = Has[DatabaseConfig]

  val getAppConfig: URIO[AppConfigEnv, AppConfig] =
    ZIO.access(_.get)

  val getHttpConfig: URIO[HttpConfigEnv, HttpConfig] =
    ZIO.access(_.get)

  val getDatabaseConfig: URIO[DatabaseConfigEnv, DatabaseConfig] =
    ZIO.access(_.get)
}
