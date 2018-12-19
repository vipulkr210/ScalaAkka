package com.vipul.akkahttp


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.event.Logging
import com.typesafe.config.{ConfigFactory, Config}
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow


case class TestSimulator(
  interface: String,
  port: Int,
  maxLimit: Int
)

object TestSimulator {
  
  import net.ceedubs.ficus.Ficus._
  def apply(): TestSimulator = apply(ConfigFactory.load.getConfig("simulator"))

  def apply(config: Config): TestSimulator = {
    new TestSimulator(
      config.as[String]("interface"),
      config.as[Int]("port"),
      config.as[Int]("maxLimit")
    )
  }
  def main(args: Array[String]) {

    implicit val system = ActorSystem("system")
    implicit val actorMaterializer = ActorMaterializer()
    val config = TestSimulator()

    val logger = Logging(system, getClass)

    val route =
      path("name") {
        post { 
         entity(as[String]) { name =>
            logger.info("Received a message" + name+"   Max Limit: "+ config.maxLimit)
            val commandResult = name.toString match {
              case "Test1" => "TEST ONE SUCCESS"
              case "Test2" => "TEST TWO SUCCESS"
              case "Test3" => "TEST THREE SUCCESS"
              case "Test4" => "TEST FOUR SUCCESS"
              case _ => "NOT MATCHED"
            }
            complete(HttpEntity(ContentTypes.`application/json`, commandResult))
          }
        
        }
      }
    println(config.port)
    Http().bindAndHandle(route,config.interface,config.port)

    println("server started at 8080")
  }

}
