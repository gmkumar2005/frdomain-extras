import Versions._
import sbt._

object Dependencies {

  object Misc {
    val refinedCore = "eu.timepit" %% "refined" % refinedVersion
    val refinedCats = "eu.timepit" %% "refined-cats" % refinedVersion
    val squants = "org.typelevel" %% "squants" % squantsVersion
  }

  object Cats {
    val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
    val catsMtl = "org.typelevel" %% "cats-mtl" % catsMtlVersion
  }


  object Ciris {
    val cirisCore = "is.cir" %% "ciris" % cirisVersion
    val cirisEnum = "is.cir" %% "ciris-enumeratum" % cirisVersion
    val cirisRefined = "is.cir" %% "ciris-refined" % cirisVersion
  }

  val commonDependencies: Seq[ModuleID] = Seq(Cats.catsEffect)

  val catsIODependencies: Seq[ModuleID] = commonDependencies

  val catsMtlDependencies: Seq[ModuleID] =
    commonDependencies ++ Seq(Cats.catsMtl) ++ Seq(Misc.refinedCore, Misc.refinedCats, Misc.squants) ++ Seq(Ciris.cirisCore, Ciris.cirisEnum, Ciris.cirisRefined)

}
