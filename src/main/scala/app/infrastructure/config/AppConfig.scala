package app.infrastructure.config

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
            s"Error loading configuration: $failures"
          )
        )
    }

}
