package app.gateway.customer.out

import app.domain.customer.Customer
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.scalaland.chimney.dsl._

final case class CustomerApiOutput(id: String, url: String, name: String, locked: Boolean)

object CustomerApiOutput {

  def apply(
             basePath: String,
             customer: Customer
           ): CustomerApiOutput =
    customer.into[CustomerApiOutput]
      .withFieldConst(_.url, s"$basePath/${customer.id.value}")
      .transform

  implicit val encoder: Encoder[CustomerApiOutput] = deriveEncoder
  implicit val decoder: Decoder[CustomerApiOutput] = deriveDecoder
}
