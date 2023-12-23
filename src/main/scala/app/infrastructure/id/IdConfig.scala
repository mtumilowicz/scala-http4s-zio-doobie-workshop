package app.infrastructure.id

import app.domain.id.{IdRepositoryEnv, IdRepository, IdService, IdServiceEnv}
import app.infrastructure.id.{DeterministicIdRepository, UuidRepository}
import zio.{URLayer, ZIO}

object IdConfig {

  val uuidRepository: URLayer[Any, IdRepositoryEnv] =
    UuidRepository.live

  val deterministicRepository: URLayer[Any, IdRepositoryEnv] =
    DeterministicIdRepository.live

  val service: URLayer[IdRepositoryEnv, IdServiceEnv] = {
    for {
      provider <- ZIO.service[IdRepository]
    } yield IdService(provider)
  }.toLayer
}
