package frdomain.ch6.domain.io.app

import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*
import cats.instances.all.*
import cats.mtl.*
import frdomain.ch6.domain.AppException
import frdomain.ch6.domain.common.Amount
import frdomain.ch6.domain.io.app.Implicits.*
import frdomain.ch6.domain.model.account.AccountType.{Checking, Savings}
import frdomain.ch6.domain.model.account.{Account, AccountName, AccountNo, Balance}
import frdomain.ch6.domain.service.{AccountService, ReportingService}
import squants.market.*

object programs {
  def programNormalOps[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
                            (implicit me: MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService.*
    import reportingService.*

    val opens =
      for {
        _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
        _ <- open(AccountNo("a2345"), AccountName("a2name"), None, None, Checking)
        _ <- open(AccountNo("a3456"), AccountName("a3name"), BigDecimal(5.8).some, None, Savings)
        _ <- open(AccountNo("a4567"), AccountName("a4name"), None, None, Checking)
        _ <- open(AccountNo("a5678"), AccountName("a5name"), BigDecimal(2.3).some, None, Savings)
      } yield (())

    val credits =
      for {
        _ <- credit(AccountNo("a1234"), USD(1000))
        _ <- credit(AccountNo("a2345"), USD(2000))
        _ <- credit(AccountNo("a3456"), USD(3000))
        _ <- credit(AccountNo("a4567"), USD(4000))
      } yield (())

    for {
      _ <- opens
      _ <- credits
      a <- balanceByAccount
    } yield a
  }

  def programCreditNonExistingAccount[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
                                           (implicit me: MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService.*
    import reportingService.*

    for {
      _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
      _ <- credit(AccountNo("a2345"), USD(2000))
      a <- balanceByAccount
    } yield a
  }

  def programInsufficientAmountToDebit[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
                                            (implicit me: MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService.*
    import reportingService.*

    for {
      _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
      _ <- credit(AccountNo("a1234"), USD(2000))
      _ <- debit(AccountNo("a1234"), USD(4000))
      a <- balanceByAccount
    } yield a
  }

  def programMultipleFails[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
                                (implicit me: MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService.*
    import reportingService.*

    for {
      a <- open(AccountNo("a134"), AccountName("a1name"), Some(BigDecimal(-0.9)), None, Savings)
      _ <- credit(a.no, USD(2000))
      _ <- debit(a.no, USD(4000))
      b <- balanceByAccount
    } yield b
  }
}
