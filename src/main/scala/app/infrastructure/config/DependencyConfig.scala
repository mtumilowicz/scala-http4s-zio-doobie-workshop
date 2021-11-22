package app.infrastructure.config

import app.domain._
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

  type RepositoryEnv =
    GatewayEnv with CustomerRepositoryEnv

  type ServiceEnv =
    RepositoryEnv with CustomerServiceEnv

  type AppEnv = ServiceEnv

  object live {

    val core: ZLayer[Blocking, Throwable, CoreEnv] =
      Blocking.any ++ AppConfig.live ++ Slf4jLogger.make((_, msg) => msg) ++ Console.live

    val gateway: ZLayer[CoreEnv, Throwable, GatewayEnv] =
      HttpConfig.fromAppConfig ++ DatabaseConfig.fromAppConfig ++ ZLayer.identity

    val repository: ZLayer[GatewayEnv, Throwable, RepositoryEnv] =
      CustomerRepository.live ++ ZLayer.identity

    val service: ZLayer[RepositoryEnv, Throwable, ServiceEnv] =
      CustomerService.live ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      core >>> gateway >>> repository >>> service
  }

  object inMemory {
    val appLayer: URLayer[Any, CustomerServiceEnv] = Console.live ++ CustomerRepository.inMemory >>> CustomerService.live
  }
}
