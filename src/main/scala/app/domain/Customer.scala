package app.domain

case class Customer(id: CustomerId, name: String, locked: Boolean)

object Customer {
  def createFrom(command: NewCustomerCommand): Customer =
    Customer(
      id = CustomerId("1cd81a0e-014e-4189-ad0c-5a1ea331d12c"),
      name = command.name,
      locked = command.locked
    )
}