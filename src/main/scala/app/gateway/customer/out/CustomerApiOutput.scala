package app.gateway.customer.out

import app.domain._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

final case class CustomerApiOutput(id: String, url: String, name: String, locked: Boolean)

object CustomerApiOutput {

  def apply(
             basePath: String,
             customer: Customer
           ): CustomerApiOutput =
    CustomerApiOutput(
      customer.id.value,
      s"$basePath/${customer.id.value}",
      customer.name,
      customer.locked
    )

  implicit val encoder: Encoder[CustomerApiOutput] = deriveEncoder
  implicit val decoder: Decoder[CustomerApiOutput] = deriveDecoder
}
