package app.domain.id

import zio.Task

trait IdRepository {
  def get: Task[String]
}
