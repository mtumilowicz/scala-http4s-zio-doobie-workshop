package app.infrastructure.config.http

import app.infrastructure.config.{AppConfigEnv, HttpConfigEnv}
import pureconfig._
import pureconfig.generic.semiauto._
import zio._

case class HttpConfig(port: Int, baseUrl: String)

object HttpConfig {

  implicit val convert: ConfigConvert[HttpConfig] = deriveConvert

  val fromAppConfig: ZLayer[AppConfigEnv, Nothing, HttpConfigEnv] =
    ZLayer.fromService(_.http)
}
