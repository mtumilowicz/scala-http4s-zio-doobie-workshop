package app.gateway.customer.out

import app.domain.customer.Customer
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.scalaland.chimney.dsl._

final case class CustomerApiOutput(id: String, name: String, locked: Boolean)

object CustomerApiOutput {

  def apply(customer: Customer): CustomerApiOutput =
    customer.transformInto[CustomerApiOutput]

  implicit val encoder: Encoder[CustomerApiOutput] = deriveEncoder
  implicit val decoder: Decoder[CustomerApiOutput] = deriveDecoder
}
