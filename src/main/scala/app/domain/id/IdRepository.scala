package app.domain.id

import zio.Task

trait IdRepository extends Serializable {
  def get: Task[String]
}
