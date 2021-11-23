package app.infrastructure.config

import app.domain._
import app.infrastructure.config.customer.CustomerConfig
import app.infrastructure.config.db.DatabaseConfig
import app.infrastructure.config.http.HttpConfig
import app.infrastructure.config.id.IdConfig
import zio.blocking.Blocking
import zio.console.Console
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{URLayer, ZLayer}

object DependencyConfig {

  type CoreEnv =
    AppConfigEnv with Logging with Blocking with Console

  type GatewayEnv =
    CoreEnv with HttpConfigEnv with DatabaseConfigEnv

  type InternalRepositoryEnv =
    GatewayEnv with IdProviderEnv

  type InternalServiceEnv =
    InternalRepositoryEnv with IdServiceEnv

  type ApiRepositoryEnv =
    InternalServiceEnv with CustomerRepositoryEnv

  type ApiServiceEnv =
    ApiRepositoryEnv with CustomerServiceEnv

  type AppEnv = ApiServiceEnv

  object live {

    val core: ZLayer[Blocking, Throwable, CoreEnv] =
      Blocking.any ++ AppConfig.live ++ Slf4jLogger.make((_, msg) => msg) ++ Console.live

    val gateway: ZLayer[CoreEnv, Throwable, GatewayEnv] =
      HttpConfig.fromAppConfig ++ DatabaseConfig.fromAppConfig ++ ZLayer.identity

    val internalRepository: ZLayer[GatewayEnv, Throwable, InternalRepositoryEnv] =
      IdConfig.uuidRepository ++ ZLayer.identity

    val internalService: ZLayer[InternalRepositoryEnv, Throwable, InternalServiceEnv] =
      IdConfig.service ++ ZLayer.identity

    val apiRepository: ZLayer[InternalServiceEnv, Throwable, ApiRepositoryEnv] =
      CustomerConfig.dbRepository ++ IdConfig.service ++ ZLayer.identity

    val apiService: ZLayer[ApiRepositoryEnv, Throwable, ApiServiceEnv] =
      CustomerConfig.service ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      core >>> gateway >>> internalRepository >>> internalService >>> apiRepository >>> apiService
  }

  object inMemory {
    val appLayer: URLayer[Any, CustomerServiceEnv] = ((IdConfig.deterministicRepository >>> IdConfig.service) ++ CustomerConfig.inMemoryRepository) >>> CustomerConfig.service
  }
}
