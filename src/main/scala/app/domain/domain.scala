package app

import app.domain.customer._

package object domain {
  type InternalRepositoryEnv = IdProviderEnv
  type InternalServiceEnv = IdServiceEnv
  type ApiRepositoryEnv = CustomerRepositoryEnv
  type ApiServiceEnv = CustomerServiceEnv
}
