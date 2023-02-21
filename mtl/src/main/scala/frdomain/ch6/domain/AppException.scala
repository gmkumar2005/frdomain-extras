package frdomain.ch6.domain

import cats.data.NonEmptyChain

trait AppException {
  def message: NonEmptyChain[String]
}


