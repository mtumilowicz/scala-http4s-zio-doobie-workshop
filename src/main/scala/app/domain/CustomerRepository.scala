package app.domain

import zio.UIO

trait CustomerRepository extends Serializable {

  def getAll: UIO[List[Customer]]

  def getById(id: CustomerId): UIO[Option[Customer]]

  def delete(id: CustomerId): UIO[CustomerId]

  def deleteAll: UIO[Unit]

  def create(customer: Customer): UIO[Customer]
}
