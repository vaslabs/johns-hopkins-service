package hopkins.covid.service

import akka.actor.typed.ActorSystem

object Bootstrap extends App{

  implicit val actorSystem = ActorSystem(Guardian.behavior, "HopkinsCovidData")

  sys.addShutdownHook {
    actorSystem.terminate()
  }
}
