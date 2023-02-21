package frdomain.ch6.domain

import cats.*
import cats.data.*
import cats.implicits.*
import cats.instances.all.*

import java.time.LocalDateTime

object common {
  type Amount = BigDecimal
  type ValidationResult[A] = ValidatedNec[String, A]
  type ErrorOr[A] = Either[NonEmptyChain[String], A]
  type MonadAppException[F[_]] = MonadError[F, AppException]

  def today = LocalDateTime.now
}

