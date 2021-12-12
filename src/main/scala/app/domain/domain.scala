package app

import app.domain.customer.{CustomerRepository, CustomerService, IdRepository, IdService}
import zio.Has

package object domain {
  type CustomerRepositoryEnv = Has[CustomerRepository]
  type CustomerServiceEnv = Has[CustomerService]
  type IdProviderEnv = Has[IdRepository]
  type IdServiceEnv = Has[IdService]

  type InternalRepositoryEnv = IdProviderEnv
  type InternalServiceEnv = IdServiceEnv
  type ApiRepositoryEnv = CustomerRepositoryEnv
  type ApiServiceEnv = CustomerServiceEnv
}
