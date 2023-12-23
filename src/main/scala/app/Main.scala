package app

import app.domain.{ApiRepositoryEnv, ApiServiceEnv, InternalRepositoryEnv, InternalServiceEnv}
import app.gateway.customer.CustomerController
import app.infrastructure.config._
import app.infrastructure.config.customer.CustomerConfig
import app.infrastructure.config.db.{DatabaseConfig, DoobieConfig, FlywayConfig}
import app.infrastructure.config.http.HttpConfig
import app.infrastructure.config.id.IdConfig
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import zio.interop.catz._
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{ExitCode => ZExitCode, _}
import zio.magic._

object Main extends App {

  type AppEnv = ZEnv
    with Logging
    with AppConfigEnv
    with HttpConfigEnv
    with DatabaseConfigEnv
    with DoobieTransactorConfigEnv
    with InternalRepositoryEnv
    with InternalServiceEnv
    with ApiRepositoryEnv
    with ApiServiceEnv

  type AppTask[A] = RIO[AppEnv, A]

  override def run(args: List[String]): URIO[ZEnv, ZExitCode] = {
    program
      .injectSome[ZEnv](
        AppConfig.live,
        Slf4jLogger.make((_, msg) => msg),
        HttpConfig.fromAppConfig,
        DatabaseConfig.fromAppConfig,
        DoobieConfig.live,
        IdConfig.uuidRepository,
        CustomerConfig.dbRepository,
        IdConfig.service,
        CustomerConfig.service
      ).orDie
  }

  val program: ZIO[AppEnv, Throwable, ZExitCode] = {
    ZIO.runtime[AppEnv]
      .flatMap { implicit runtime =>
        for {
          appConfig <- getAppConfig
          HttpConfig(port, baseUrl) = appConfig.http
          databaseConfig = appConfig.database
          _ <- logging.log.info(s"Migrating db with flyway")
          _ <- FlywayConfig.initDb(databaseConfig)
          _ <- logging.log.info(s"Starting with $baseUrl")
          _ <- BlazeServerBuilder.apply[AppTask](runtime.platform.executor.asEC)
            .bindHttp(port, "0.0.0.0")
            .withHttpApp(routes(baseUrl))
            .serve
            .compile
            .drain
        } yield ZExitCode.success
      }

  }

  def routes(baseUrl: String) =
    Router[AppTask](
      customerHttp(baseUrl)
    ).orNotFound

  def customerHttp(baseUrl: String): (String, HttpRoutes[AppTask]) =
    "/customers" -> new CustomerController().routes(s"$baseUrl/customers")
}