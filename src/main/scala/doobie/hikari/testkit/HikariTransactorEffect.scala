package doobie.hikari.testkit

import cats.effect.{ Blocker, ContextShift, IO, Resource }
import com.zaxxer.hikari.HikariDataSource
import doobie.KleisliInterpreter
import doobie.hikari.HikariTransactor
import doobie.util.transactor.{ Strategy, Transactor }

import java.sql.Connection
import scala.concurrent.ExecutionContext

/**  Creates test resources required to run Doobie Hikari framework for unit tests.
  *
  *  <p>Instantiates [[ContextShift]] to allow [[Transactor]] instantiation.
  *
  *  <p>Instantiates mock [[HikariDataSource]] to allow to separate data source
  *  layer from the testing context.
  *
  *  <p>Instantiates auxiliary [[Transactor]] with mock resources to allow to operate
  *  mock connections on a thread pool.
  *
  *  <p>Finally, instantiates a [[Resource]] transactor, mockTransactor. This is not an
  *  instance of a mock, but conveys such name to clearly indicate the meaning of
  *  the variable is for testing purposes. It should be used as a [[Transactor]] required
  *  to run database transactions, instead this is going to completely mock the database resource.
  *
  *  @param ec that is required to run mock connections
  *  @author Norbert Gogiel
  *  @since 0.1
  */
private[doobie] class HikariTransactorEffect(ec: ExecutionContext) extends TestJavaSqlConnection {
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  val mockHikariDataSource: HikariDataSource = mock[HikariDataSource]
  val transactor: Transactor.Aux[IO, HikariDataSource] = Transactor(
    mockHikariDataSource,
    (_: HikariDataSource) => Resource.pure[IO, Connection](mockConnection),
    KleisliInterpreter[IO](Blocker.liftExecutionContext(ExecutionContext.global)).ConnectionInterpreter,
    Strategy.void
  )
  val resourceRelease: HikariTransactor[IO] => IO[Unit] = (_: HikariTransactor[IO]) => IO.unit
  val mockTransactor: Resource[IO, HikariTransactor[IO]] =
    Resource.make[IO, HikariTransactor[IO]](IO.pure(transactor))(resourceRelease)
}
