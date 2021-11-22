package app.domain

import app.infrastructure.id.{DeterministicStringProvider, UuidProvider}
import zio.{Task, URLayer}


trait IdProvider extends Serializable {
  def get: Task[String]
}

object IdProvider {
  val live: URLayer[Any, IdProviderEnv] =
    UuidProvider.live

  val deterministic: URLayer[Any, IdProviderEnv] =
    DeterministicStringProvider.live
}
