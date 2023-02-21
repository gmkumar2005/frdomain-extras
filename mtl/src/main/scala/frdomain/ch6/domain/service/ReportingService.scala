package frdomain.ch6.domain.service

import frdomain.ch6.domain.model.account.AccountNo
import squants.market.*

trait ReportingService[M[_], Amount] {
  def balanceByAccount: M[Seq[(AccountNo, Money)]]
} 

