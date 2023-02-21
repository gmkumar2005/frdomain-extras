package frdomain.ch6.domain.io.app

import cats.*
import cats.data.*
import cats.effect.IO
import cats.effect.implicits.*
import cats.implicits.*
import cats.instances.all.*
import cats.mtl.*
import frdomain.ch6.domain.AppException

object Implicits {

  implicit val monadErrorServiceIO: MonadError[IO, AppException] =

    new MonadError[IO, AppException] {

      def pure[A](a: A) = IO.pure(a)

      def flatMap[A, B](fa: IO[A])(f: A => IO[B]) = fa.flatMap(f)

      def tailRecM[A, B](a: A)(f: A => IO[Either[A, B]]): IO[B] = TailRecM.defaultTailRecM(a)(f)

      def raiseError[A](e: AppException): IO[A] = IO.raiseError(new Exception(e.message.toList.mkString("/")))

      def handleErrorWith[A](fio: IO[A])(f: AppException => IO[A]): IO[A] = ???

    }

  //  object DefaultApplicativeAsk {
  //
  //    def constant[F[_]: Applicative, E](e: E): Ask[F, E] = {
  //      new DefaultApplicativeAsk[F, E] {
  //        val applicative: Applicative[F] = Applicative[F]
  //        def ask: F[E] = applicative.pure(e)
  //      }
  //    }
  //
  //  }

  object TailRecM {

    def defaultTailRecM[F[_], A, B](a: A)(f: A => F[Either[A, B]])
                                   (implicit F: Monad[F]): F[B] =

      F.flatMap(f(a)) {
        case Left(a2) => defaultTailRecM(a2)(f)
        case Right(b) => F.pure(b)
      }
  }
}
