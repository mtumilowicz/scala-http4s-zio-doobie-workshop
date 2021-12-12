package app.infrastructure.id

import app.domain.customer._
import zio.{Task, URLayer, ZLayer}

import java.util.UUID

private class UuidRepository extends IdRepository {
  override def get: Task[String] = Task.succeed(UUID.randomUUID().toString)
}

object UuidRepository {
  val live: URLayer[Any, IdProviderEnv] =
    ZLayer.succeed(new UuidRepository())
}