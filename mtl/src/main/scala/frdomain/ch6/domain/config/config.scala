package frdomain.ch6.domain.config

import ciris.*
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

object config {
  case class AppConfig(
                        postgreSQL: PostgreSQLConfig
                      )

  case class PostgreSQLConfig(
                               host: NonEmptyString,
                               port: UserPortNumber,
                               user: NonEmptyString,
                               database: NonEmptyString,
                               max: PosInt
                             )
}
