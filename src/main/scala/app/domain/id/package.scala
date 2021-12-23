package app.domain

import zio.Has

package object id {
  type IdProviderEnv = Has[IdRepository]
  type IdServiceEnv = Has[IdService]
}
