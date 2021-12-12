package app.infrastructure.config.id

import app.domain.customer._
import app.infrastructure.id.{DeterministicIdRepository, UuidRepository}
import zio.{URLayer, ZIO}

object IdConfig {

  val uuidRepository: URLayer[Any, IdProviderEnv] =
    UuidRepository.live

  val deterministicRepository: URLayer[Any, IdProviderEnv] =
    DeterministicIdRepository.live

  val service: URLayer[IdProviderEnv, IdServiceEnv] = {
    for {
      provider <- ZIO.service[IdRepository]
    } yield IdService(provider)
  }.toLayer
}
