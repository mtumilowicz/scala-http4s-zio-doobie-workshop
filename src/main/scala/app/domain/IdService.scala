package app.domain

import zio.Task

case class IdService(provider: IdRepository) {
  def generate(): Task[String] = provider.get
}