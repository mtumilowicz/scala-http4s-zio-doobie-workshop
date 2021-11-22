package app.domain

import zio.{Task, URLayer, ZIO}

case class IdService(provider: IdProvider) {
  def generate(): Task[String] = provider.get
}

object IdService {
  val live: URLayer[IdProviderEnv, IdServiceEnv] = {
    for {
      provider <- ZIO.service[IdProvider]
    } yield IdService(provider)
  }.toLayer
}