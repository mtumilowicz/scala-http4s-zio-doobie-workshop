package app

import app.domain.customer.{CustomerRepositoryEnv, CustomerServiceEnv}
import app.domain.id.{IdRepositoryEnv, IdServiceEnv}

package object domain {
  type InternalRepositoryEnv = IdRepositoryEnv
  type InternalServiceEnv = IdServiceEnv
  type ApiRepositoryEnv = CustomerRepositoryEnv
  type ApiServiceEnv = CustomerServiceEnv
}
