package hopkins.database.github

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import hopkins.database.github.CountryStatsAggregation.Protocol._

object WorldStatsAggregation {


  val behaviour: Behavior[CountryStatsAggregation.Protocol] = Behaviors.receive {
    case (ctx, s @ AddCountryStats(countryStats, _)) =>
      ctx.log.info("Adding stats for {}", countryStats.country.name)
      val name = countryStats.country.name.replaceAll("\\W", "-")
      val countryStatsAggregation =
        ctx.child(name)
          .getOrElse(
            ctx.spawn(
              CountryStatsAggregation.behaviour(Map.empty), name)
          ).unsafeUpcast[CountryStatsAggregation.Protocol]

      countryStatsAggregation ! s
      Behaviors.same
    case (ctx, gcs @ GetCountryStats(country, _, _, replyTo)) =>
      val name = country.name.replaceAll("\\W", "-")
      ctx.child(name) match {
        case Some(child) =>
          child.unsafeUpcast[CountryStatsAggregation.Protocol] ! gcs
        case None =>
          replyTo ! Left(())
      }

      Behaviors.same
    case (ctx, Completed) =>
      ctx.log.info("Initialisation completed")
      Behaviors.same
    case (ctx, Failure(t)) =>
      ctx.log.error("Error loading data", t)
      Behaviors.same
    case (_, Start(replyTo: ActorRef[Ack])) =>
      replyTo ! Ack
      Behaviors.same


  }


}
