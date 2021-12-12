package app.domain.customer

import zio.Task

trait IdRepository extends Serializable {
  def get: Task[String]
}
