package app.gateway

import _root_.app.gateway.HttpTestUtils.makeRequest
import app.gateway.CustomerControllerSpec.CustomerTask
import cats.data.Kleisli
import io.circe.Json
import org.http4s.circe.jsonEncoder
import org.http4s.{Method, Request, Response}

object CustomerLifecycle {

  type AppEndpoints = Kleisli[CustomerTask, Request[CustomerTask], Response[CustomerTask]]

  def create(json: Json)(implicit app: AppEndpoints): CustomerTask[Response[CustomerTask]] = {
    val request = makeRequest[CustomerTask](Method.POST, "/")
      .withEntity(json)

    app.run(request)
  }

  def getAll(implicit app: AppEndpoints): CustomerTask[Response[CustomerTask]] = {
    val request = makeRequest[CustomerTask](Method.GET, "/")

    app.run(request)
  }

  def getById(id: String)(implicit app: AppEndpoints): CustomerTask[Response[CustomerTask]] = {
    val request = makeRequest[CustomerTask](Method.GET, s"/$id")

    app.run(request)
  }

  def deleteById(id: String)(implicit app: AppEndpoints): CustomerTask[Response[CustomerTask]] = {
    val request = makeRequest[CustomerTask](Method.DELETE, s"/$id")

    app.run(request)
  }

  def deleteAll(implicit app: AppEndpoints): CustomerTask[Response[CustomerTask]] = {
    val request = makeRequest[CustomerTask](Method.DELETE, "/")

    app.run(request)
  }

}
