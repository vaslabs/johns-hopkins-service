import sbt._
object Dependencies {

  object Version {

    object akka {
      val core = "2.6.4"
      val http = "10.1.11"
    }

    object alpakka {
      val core = "2.0.0-RC1"
    }

    object cats {
      val kittens = "2.0.0"
    }

    object scalatest {
      val core = "3.1.1"
    }
    object tapir {
      val core = "0.12.25"
    }

    object circe {
      val core = "0.13.0"
    }
    val github4s = "0.23.0"

    val logbackClassic = "1.2.3"

  }

  object Library {

    object akka {
      val actors = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka.core
      val streams = "com.typesafe.akka" %% "akka-stream-typed" % Version.akka.core
      val http = "com.typesafe.akka" %% "akka-http"   % Version.akka.http
    }
    object alpakka {
      val csv = "com.lightbend.akka" %% "akka-stream-alpakka-csv" % Version.alpakka.core
    }

    object cats {
      val kittens = "org.typelevel" %% "kittens" % Version.cats.kittens
    }

    object circe {
      val all = Seq("io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-generic-extras",
        "io.circe" %% "circe-parser").map(_ % Version.circe.core)
    }
    object scalatest {
      val core =  "org.scalatest" %% "scalatest" % Version.scalatest.core % "test"
    }

    object Logback {
      val essentials = Seq(
        "ch.qos.logback" % "logback-classic",
        "ch.qos.logback" % "logback-core",
        "ch.qos.logback" % "logback-access"
      ).map(_  % Version.logbackClassic)
    }

    val github4s = "com.47deg" %% "github4s" % Version.github4s

    object tapir {
      val core = "com.softwaremill.sttp.tapir" %% "tapir-core" % Version.tapir.core
      val akka = "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % Version.tapir.core
      val circe = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Version.tapir.core

      val docs = Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs",
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml",
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http"
      ).map(_ % Version.tapir.core)
    }
  }

  object Module {
    import Library._

    val endpoints = Seq(scalatest.core, tapir.core, tapir.circe) ++ circe.all
    val database = Seq(
        scalatest.core,
        github4s,
        akka.streams,
        akka.http,
        alpakka.csv,
        akka.actors,
        cats.kittens
    ) ++ circe.all

    val service = Seq(tapir.akka) ++ tapir.docs ++ Logback.essentials

  }
}
