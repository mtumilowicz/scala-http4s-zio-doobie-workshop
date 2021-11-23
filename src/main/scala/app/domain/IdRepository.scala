package app.domain

import zio.Task


trait IdRepository extends Serializable {
  def get: Task[String]
}
