import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorSystem
import akka.actor.ActorRef
import java.io.File
import com.typesafe.config.ConfigFactory
import akka.pattern.{ask, pipe}
import akka.actor.ActorLogging
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

case class StartAppointment(branch: ActorRef, clerkId: Int)
case class BalanceRequest(visitorId: Int)
case class BalanceResponse(balance: Int, clerkId: Int)

class Visitor(id: Int) extends Actor with ActorLogging {
  def receive = {
    case StartAppointment(branch, clerkId) => {
      log.info(s"User[$id] is asking Clerk[${clerkId}] for balance")
      branch ! BalanceRequest(id)
    }
    case BalanceResponse(balance, clerkId) => {
      log.info(s"User[$id] Received balance request from Clerk[${clerkId}]: $balance €")
      context.stop(self)
    }
    case _ => log.info("Visitor does not undersrand response")
  }
}

class BankClerk(id: Int) extends Actor {
  val log = Logging(context.system, this)
  val r   = scala.util.Random
  def receive = {
    case BalanceRequest(visitorNumber) => {
      log.info(s"Clerk[$id] received balance request from User[$visitorNumber]")
      Thread.sleep(2000)
      log.info(s"Clerk[$id] processed balance request for User[$visitorNumber]")
      sender() ! BalanceResponse(r.nextInt(), id)
    }
    case _ => log.info("Branch does not understand request")
  }
}

object Bank {
  def main(args: Array[String]): Unit = {
    val system                    = ActorSystem("Bank")
    implicit val timeout          = Timeout(5, TimeUnit.MINUTES)

    val visitorsAppontments = (1 to 10)

    val visitorsFutures = visitorsAppontments.map(id => {
      val bankClerkActor = system.actorOf(Props(new BankClerk(id)).withDispatcher("clerks"), s"clerk-${id}")
      val visitorActor = system.actorOf(Props(new Visitor(id)).withDispatcher("visitors"), s"visitor-${id}")
      (visitorActor ? StartAppointment(bankClerkActor, id)).mapTo[String]
    })

    val results = Await.result(Future.sequence(visitorsFutures), 5.minute)
    results.foreach(println(_))
    system.terminate()
  }
}
