package app

import app.domain.customer.{CustomerRepositoryEnv, CustomerServiceEnv}
import app.domain.id.{IdProviderEnv, IdServiceEnv}

package object domain {
  type InternalRepositoryEnv = IdProviderEnv
  type InternalServiceEnv = IdServiceEnv
  type ApiRepositoryEnv = CustomerRepositoryEnv
  type ApiServiceEnv = CustomerServiceEnv
}
