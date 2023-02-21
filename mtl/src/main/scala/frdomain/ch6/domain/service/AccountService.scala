package frdomain.ch6.domain.service


import frdomain.ch6.domain.model.account.{AccountName, AccountNo, AccountType}
import squants.market.*

import java.time.LocalDateTime

abstract class AccountService[M[_], Acc, Amount, Bal] {

  def open(no: AccountNo, name: AccountName, rate: Option[BigDecimal], openingDate: Option[LocalDateTime],
           accountType: AccountType): M[Acc]

  def close(no: AccountNo, closeDate: Option[LocalDateTime]): M[Acc]

  def debit(no: AccountNo, amount: Money): M[Acc]

  def credit(no: AccountNo, amount: Money): M[Acc]

  def balance(no: AccountNo): M[Option[Bal]]

  def transfer(from: AccountNo, to: AccountNo, amount: Money): M[(Acc, Acc)]
}