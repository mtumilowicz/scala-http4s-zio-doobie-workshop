package app.infrastructure.config

import app.infrastructure.db.DatabaseConfig
import app.infrastructure.http.HttpConfig
import cats.implicits.showInterpolator
import pureconfig._
import pureconfig.generic.semiauto._
import zio._

final case class AppConfig(http: HttpConfig, database: DatabaseConfig)

object AppConfig {

  implicit val convert: ConfigConvert[AppConfig] = deriveConvert

  val live: ZLayer[Any, IllegalStateException, AppConfigEnv] =
    ZLayer.fromEffect {
      ZIO
        .fromEither(ConfigSource.default.load[AppConfig])
        .mapError(failures =>
          new IllegalStateException(
            show"Error loading configuration: ${failures.prettyPrint()}"
          )
        )
    }

}
