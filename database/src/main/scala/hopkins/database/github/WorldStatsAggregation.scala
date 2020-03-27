package hopkins.database.github

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import hopkins.database.github.CountryStatsAggregation.Protocol._

object WorldStatsAggregation {


  val behaviour: Behavior[CountryStatsAggregation.Protocol] = Behaviors.receive {
    case (ctx, s @ AddCountryStats(countryStats, _)) =>
      val countryStatsAggregation =
        ctx.child(countryStats.country.name)
          .getOrElse(
            ctx.spawn(
              CountryStatsAggregation.behaviour(Map.empty), countryStats.country.name)
          ).unsafeUpcast[CountryStatsAggregation.Protocol]

      countryStatsAggregation ! s
      Behaviors.same
    case (ctx, Completed) =>
      ctx.log.info("Initialisation completed")
      Behaviors.same
    case (_, Start(replyTo: ActorRef[Ack])) =>
      replyTo ! Ack
      Behaviors.same

  }


}
