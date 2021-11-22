package app.domain

case class NewCustomerCommand(name: String, locked: Boolean) {
  def toCustomer(id: CustomerId): Customer =
    Customer(id = id, name = name, locked = locked)
}
