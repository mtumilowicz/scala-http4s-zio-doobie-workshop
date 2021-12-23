package app.domain

import zio.Has

package object customer {
  type CustomerRepositoryEnv = Has[CustomerRepository]
  type CustomerServiceEnv = Has[CustomerService]
}
