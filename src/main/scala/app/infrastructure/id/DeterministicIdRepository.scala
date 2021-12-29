package app.infrastructure.id

import app.domain.id.{IdRepositoryEnv, IdRepository}
import zio.{Ref, Task, URLayer, ZLayer}

private class DeterministicIdRepository(counter: Ref[Int]) extends IdRepository {
  override def get: Task[String] = counter.updateAndGet(_ + 1).map(_.toString)
}

object DeterministicIdRepository {
  val live: URLayer[Any, IdRepositoryEnv] =
    ZLayer.fromEffect {
      for {
        counter <- Ref.make(0)
      } yield new DeterministicIdRepository(counter)
    }
}
