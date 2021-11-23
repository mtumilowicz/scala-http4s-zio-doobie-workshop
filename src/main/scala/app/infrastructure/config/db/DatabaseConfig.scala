package app.infrastructure.config.db

import app.infrastructure.config.{AppConfigEnv, DatabaseConfigEnv}
import pureconfig._
import pureconfig.generic.semiauto._
import zio._

final case class DatabaseConfig(url: String, driver: String, user: String, password: String)

object DatabaseConfig {

  implicit val convert: ConfigConvert[DatabaseConfig] = deriveConvert


  val fromAppConfig: ZLayer[AppConfigEnv, Nothing, DatabaseConfigEnv] =
    ZLayer.fromService(_.database)
}
