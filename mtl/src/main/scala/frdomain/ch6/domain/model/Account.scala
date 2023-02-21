package frdomain.ch6
package domain
package model

import cats.*
import cats.data.*
import cats.implicits.*
import cats.instances.all.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{Uuid, ValidBigDecimal}
import eu.timepit.refined.types.string.NonEmptyString

import java.time.LocalDateTime
import scala.language.postfixOps
//import _root_.io.estatico.newtype.macros.newtype
import enumeratum.*
import enumeratum.EnumEntry.*
import frdomain.ch6.domain.common.*
import squants.market.*

object account {

  case class AccountNo(value: String)

  case class AccountName(value: String)

  final val ZERO_USD = USD(BigDecimal(0.0))

  case class Balance(amount: Money = ZERO_USD)

  final val ZERO_BALANCE = Balance()

  sealed trait Account {
    def no: AccountNo

    def name: AccountName

    def dateOfOpen: Option[LocalDateTime]

    def dateOfClose: Option[LocalDateTime]

    def balance: Balance
  }

  final case class CheckingAccount(no: AccountNo, name: AccountName,
                                   dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = ZERO_BALANCE) extends Account

  final case class SavingsAccount(no: AccountNo, name: AccountName, rateOfInterest: Amount,
                                  dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = ZERO_BALANCE) extends Account

  object Account {

    private def validateAccountNo(no: AccountNo): ValidationResult[AccountNo] =
      if (no.value.isEmpty || no.value.size < 5) s"Account No has to be at least 5 characters long: found $no".invalidNec
      else no.validNec

    private def validateOpenCloseDate(od: LocalDateTime,
                                      cd: Option[LocalDateTime]): ValidationResult[(Option[LocalDateTime], Option[LocalDateTime])] = cd.map { c =>

      if (c isBefore od) s"Close date [$c] cannot be earlier than open date [$od]".invalidNec
      else (od.some, cd).validNec
    }.getOrElse {
      (od.some, cd).validNec
    }

    private def validateRate(rate: BigDecimal): ValidationResult[BigDecimal] =
      if (rate <= BigDecimal(0)) s"Interest rate $rate must be > 0".invalidNec
      else rate.validNec

    def checkingAccount(no: AccountNo, name: AccountName, openDate: Option[LocalDateTime], closeDate: Option[LocalDateTime],
                        balance: Balance): ErrorOr[Account] = {

      (
        validateAccountNo(no),
        validateOpenCloseDate(openDate.getOrElse(today), closeDate)

      ).mapN { (n, d) =>
        CheckingAccount(n, name, d._1, d._2, balance)
      }.toEither
    }

    def savingsAccount(no: AccountNo, name: AccountName, rate: BigDecimal, openDate: Option[LocalDateTime],
                       closeDate: Option[LocalDateTime], balance: Balance): ErrorOr[Account] = {

      (
        validateAccountNo(no),
        validateOpenCloseDate(openDate.getOrElse(today), closeDate),
        validateRate(rate)

      ).mapN { (n, d, r) =>
        SavingsAccount(n, name, r, d._1, d._2, balance)
      }.toEither
    }

    private def validateAccountAlreadyClosed(a: Account): ValidationResult[Account] = {
      a.validNec
    }

    private def validateCloseDate(a: Account, cd: LocalDateTime): ValidationResult[LocalDateTime] = {
      if (cd isBefore a.dateOfOpen.get) s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]".invalidNec
      else cd.validNec
    }

    def close(a: Account, closeDate: LocalDateTime): ErrorOr[Account] = {
      (validateAccountAlreadyClosed(a), validateCloseDate(a, closeDate)).mapN { (acc, _) =>
        acc match {
          case c: CheckingAccount => c.copy(dateOfClose = Some(closeDate))
          case s: SavingsAccount => s.copy(dateOfClose = Some(closeDate))
        }
      }.toEither
    }

    private def checkBalance(a: Account, amount: Money): ValidationResult[Account] = {
      //      val someDollars = Money(BigDecimal(-1.0))
      if (amount < ZERO_USD && a.balance.amount < amount) s"Insufficient amount in ${a.no} to debit".invalidNec
      else a.validNec
    }

    def updateBalance(a: Account, amount: Money): ErrorOr[Account] = {
      (validateAccountAlreadyClosed(a), checkBalance(a, amount)).mapN { (_, _) =>
        a match {
          case c: CheckingAccount => c.copy(balance = Balance(c.balance.amount + amount))
          case s: SavingsAccount => s.copy(balance = Balance(s.balance.amount + amount))
        }
      }.toEither
    }

    def rate(a: Account) = a match {
      case SavingsAccount(_, _, r, _, _, _) => r.some
      case _ => None
    }
  }

  sealed trait AccountType extends EnumEntry

  object AccountType extends Enum[AccountType] {

    case object Checking extends AccountType

    case object Savings extends AccountType

    val values = findValues
  }
}