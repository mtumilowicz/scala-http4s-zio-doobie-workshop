package app.infrastructure.id

import app.domain.{IdProvider, IdProviderEnv}
import zio.{Ref, Task, URLayer, ZLayer}

private class DeterministicStringProvider(counter: Ref[Int]) extends IdProvider {
  override def get: Task[String] = counter.updateAndGet(_ + 1).map(_.toString)
}

object DeterministicStringProvider {
  val live: URLayer[Any, IdProviderEnv] =
    ZLayer.fromEffect {
      for {
        counter <- Ref.make(0)
      } yield new DeterministicStringProvider(counter)
    }
}
