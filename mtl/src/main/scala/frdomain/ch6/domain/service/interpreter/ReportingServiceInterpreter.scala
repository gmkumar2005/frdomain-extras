package frdomain.ch6.domain.service.interpreter

import cats.*
import cats.implicits.*
import cats.mtl.*
import frdomain.ch6.domain.AppException
import frdomain.ch6.domain.common.*
import frdomain.ch6.domain.model.account.AccountNo
import frdomain.ch6.domain.repository.AccountRepository
import frdomain.ch6.domain.service.ReportingService
import squants.market.*

class ReportingServiceInterpreter[M[+_]]
(implicit E: MonadError[M, AppException], A: Ask[M, AccountRepository[M]])
  extends ReportingService[M, Amount] {

  def balanceByAccount: M[Seq[(AccountNo, Money)]] = for {

    repo <- A.ask
    allBalances <- repo.all
    balances <- allBalances.map(account => (account.no, account.balance.amount)).pure[M]

  } yield balances
} 

