package app.gateway.customer.in

import app.domain.customer.NewCustomerCommand
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.scalaland.chimney.dsl._

case class NewCustomerApiInput(name: String) {

  def toDomain: NewCustomerCommand = this.into[NewCustomerCommand]
    .withFieldConst(_.locked, false)
    .transform
}

object NewCustomerApiInput {
  implicit val decoder: Decoder[NewCustomerApiInput] = deriveDecoder
}