package app.infrastructure.db

import org.flywaydb.core.Flyway
import zio.Task

object FlywayConfig {

  def initDb(cfg: DatabaseConfig): Task[Unit] =
    Task {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
        .migrate()
    }.unit

}
