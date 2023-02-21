package frdomain.ch6.domain.repository.interpreter

import cats.*
import cats.data.*
import cats.effect.{Ref, Sync}
import cats.implicits.*
import cats.instances.all.*
import frdomain.ch6.domain.common.*
import frdomain.ch6.domain.model.account.{Account, AccountNo, Balance}
import frdomain.ch6.domain.repository.AccountRepository

import java.time.LocalDateTime
import scala.collection.immutable.Map

// Constructor private for the interpreter to prevent the Ref from leaking
// access through smart constructor below
final class AccountRepositoryInMemory[M[_] : Monad] private(repo: Ref[M, Map[AccountNo, Account]])
  extends AccountRepository[M] {

  def query(no: AccountNo): M[Option[Account]] = repo.get.map(_.get(no))

  def store(a: Account): M[Account] = repo.update(_ + ((a.no, a))).map(_ => a)

  def query(openedOn: LocalDateTime): M[List[Account]] =
    repo.get.map(_.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toList)

  def all: M[List[Account]] = repo.get.map(_.values.toList)

  def balance(no: AccountNo): M[Option[Balance]] = query(no).map(_.map(_.balance))
}

// Smart constructor 
object AccountRepositoryInMemory {
  def make[M[_] : Sync]: M[AccountRepositoryInMemory[M]] =
    Ref.of[M, Map[AccountNo, Account]](Map.empty).map(new AccountRepositoryInMemory(_))
}
