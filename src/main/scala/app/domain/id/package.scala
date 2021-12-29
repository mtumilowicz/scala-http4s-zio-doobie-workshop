package app.domain

import zio.Has

package object id {
  type IdRepositoryEnv = Has[IdRepository]
  type IdServiceEnv = Has[IdService]
}
