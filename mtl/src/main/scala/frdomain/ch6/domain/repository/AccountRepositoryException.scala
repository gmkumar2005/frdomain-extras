package frdomain.ch6.domain.repository

import cats.*
import cats.data.*
import cats.instances.all.*
import frdomain.ch6.domain.AppException
import frdomain.ch6.domain.model.account.AccountNo

trait AccountRepositoryException extends AppException

case class NonExistingAccount(no: AccountNo) extends AccountRepositoryException {
  val message = NonEmptyChain(s"No existing account with no $no")
}
