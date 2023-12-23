package app.infrastructure

import app.infrastructure.db.DatabaseConfig
import app.infrastructure.http.HttpConfig
import doobie.Transactor
import zio._

package object config {
  type AppConfigEnv = Has[AppConfig]
  type HttpConfigEnv = Has[HttpConfig]
  type DatabaseConfigEnv = Has[DatabaseConfig]
  type DoobieTransactorConfigEnv = Has[Transactor[Task]]
}
