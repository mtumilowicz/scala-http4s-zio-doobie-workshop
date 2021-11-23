package app

import zio.Has

package object domain {
  type CustomerRepositoryEnv = Has[CustomerRepository]
  type CustomerServiceEnv = Has[CustomerService]
  type IdProviderEnv = Has[IdRepository]
  type IdServiceEnv = Has[IdService]
}
