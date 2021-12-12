package app.domain

import zio.Has

package object customer {
  type CustomerRepositoryEnv = Has[CustomerRepository]
  type CustomerServiceEnv = Has[CustomerService]
  type IdProviderEnv = Has[IdRepository]
  type IdServiceEnv = Has[IdService]
}
