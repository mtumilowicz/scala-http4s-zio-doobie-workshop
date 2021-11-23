package app.infrastructure.id

import app.domain.{IdProviderEnv, IdRepository}
import zio.{Ref, Task, URLayer, ZLayer}

private class DeterministicIdRepository(counter: Ref[Int]) extends IdRepository {
  override def get: Task[String] = counter.updateAndGet(_ + 1).map(_.toString)
}

object DeterministicIdRepository {
  val live: URLayer[Any, IdProviderEnv] =
    ZLayer.fromEffect {
      for {
        counter <- Ref.make(0)
      } yield new DeterministicIdRepository(counter)
    }
}
