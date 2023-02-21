package frdomain.ch6
package domain
package io
package app

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.mtl.Ask
import frdomain.ch6.domain.common.*
import frdomain.ch6.domain.model.account.*
import frdomain.ch6.domain.model.account.AccountType.*
import frdomain.ch6.domain.repository.AccountRepository
import frdomain.ch6.domain.repository.interpreter.AccountRepositoryInMemory
import frdomain.ch6.domain.service.interpreter.*
import frdomain.ch6.domain.service.{AccountService, ReportingService}
import squants.market.*

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      _ <- UseCase1().flatMap(data => IO(Console.println("Data1 :: " + data)))
        .handleErrorWith(d1err => IO(Console.println("Data1 Errro:: " + d1err)))
      _ <- UseCase2().flatMap(data => IO(Console.println("Data2 :: " + data)))
        .handleErrorWith(d1err => IO(Console.println("Data2 Errro:: " + d1err)))
      _ <- UseCase3().flatMap(data => IO(Console.println("Data3 :: " + data)))
        .handleErrorWith(d1err => IO(Console.println("Data3 Errro:: " + d1err)))
      _ <- UseCase4().flatMap(data => IO(Console.println("Data4 :: " + data)))
        .handleErrorWith(d1err => IO(Console.println("Data4 Errro:: " + d1err)))
    } yield (ExitCode.Success)


  }

  // Expected output
  //  Data1 :: List((a5678, 0 USD), (a3456, 3E+3 USD), (a1234, 1E+3 USD), (a2345, 2E+3 USD), (a4567, 4E+3 USD))
  //  Data2 Errro :: java .lang.Exception: No existing account with no a2345
  //  Data3 Errro :: java .lang.Exception: Insufficient amount in a1234 to debit
  //  Data4 Errro :: java .lang.Exception: Account No has to be at least 5 characters long: found a134 / Interest rate - 0.9 must be > 0
  // Result after scala3 migration
  //  Data1 :: List((AccountNo(a1234), 1E+3 USD), (AccountNo(a2345), 2E+3 USD), (AccountNo(a4567), 4E+3 USD), (AccountNo(a3456), 3E+3 USD), (AccountNo(a5678), 0 USD))
  //  Data2 Errro :: java.lang.Exception: No existing account with no AccountNo (a2345)
  //  Data3 :: List((AccountNo(a1234), 6E+3 USD))
  //  Data4 Errro :: java.lang.Exception: Account No has to be at least 5 characters long: found AccountNo (a134) / Interest rate -0.9 must be > 0
  object UseCase1 {

    def apply() = {
      import Implicits.*
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        given repositoryAsk: Ask[IO, AccountRepository[IO]] = Ask.const[IO, AccountRepository[IO]](repo)

        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
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
      } yield {
        a
      }
    }
  }

  object UseCase2 {

    def apply() = {
      import Implicits.*
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        given repositoryAsk: Ask[IO, AccountRepository[IO]] = Ask.const[IO, AccountRepository[IO]](repo)

        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
                     (implicit me: MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

      import accountService.*
      import reportingService.*

      for {
        _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
        _ <- credit(AccountNo("a2345"), USD(2000))
        a <- balanceByAccount
      } yield a
    }
  }

  object UseCase3 {

    def apply() = {
      import Implicits.*
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        given repositoryAsk: Ask[IO, AccountRepository[IO]] = Ask.const[IO, AccountRepository[IO]](repo)

        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
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
  }

  object UseCase4 {

    def apply() = {
      import Implicits.*
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        given repositoryAsk: Ask[IO, AccountRepository[IO]] = Ask.const[IO, AccountRepository[IO]](repo)

        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
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
}
