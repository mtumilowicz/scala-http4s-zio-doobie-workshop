package app

import zio.Has

package object domain {
  type CustomerRepositoryEnv = Has[CustomerRepository]
  type CustomerServiceEnv = Has[CustomerService]
  type IdProviderEnv = Has[IdProvider]
  type IdServiceEnv = Has[IdService]
}
