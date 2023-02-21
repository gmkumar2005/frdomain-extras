package frdomain.ch6.domain.repository

import cats.*
import cats.data.*
import cats.instances.all.*
import frdomain.ch6.domain.model.account.{Account, AccountNo, Balance}

import java.time.LocalDateTime

trait AccountRepository[M[_]] {
  def query(no: AccountNo): M[Option[Account]]

  def store(a: Account): M[Account]

  def query(openedOn: LocalDateTime): M[List[Account]]

  def all: M[List[Account]]

  def balance(no: AccountNo): M[Option[Balance]]
}
