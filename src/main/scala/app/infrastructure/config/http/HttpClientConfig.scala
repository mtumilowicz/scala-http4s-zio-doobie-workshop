package app.infrastructure.config.http

import app.infrastructure.config.HttpClientConfigEnv
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.interop.catz._
import zio.{Task, TaskManaged, ZIO, ZLayer}

object HttpClientConfig {

  private def makeHttpClient: TaskManaged[Client[Task]] =
    ZIO.runtime[Any].map { implicit rts =>
      BlazeClientBuilder
        .apply[Task](rts.platform.executor.asEC)
        .resource
        .toManaged
    }.toManaged_.flatten

  def live: ZLayer[Any, Throwable, HttpClientConfigEnv] =
    makeHttpClient.toLayer

}
