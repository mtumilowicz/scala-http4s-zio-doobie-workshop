package app.infrastructure.id

import app.domain.{IdProvider, IdProviderEnv}
import zio.{Task, URLayer, ZLayer}

import java.util.UUID

private class UuidProvider extends IdProvider {
  override def get: Task[String] = Task.succeed(UUID.randomUUID().toString)
}

object UuidProvider {
  val live: URLayer[Any, IdProviderEnv] =
    ZLayer.succeed(new UuidProvider())
}