package doobie.hikari.testkit

import org.scalamock.handlers.CallHandler2

import scala.concurrent.ExecutionContext

/** Creates test resources required to run Doobie Hikari framework for unit tests.
  * It allows to quickly test database transactions without needing to create test
  * databases.
  *
  * <p> The abstract class exposes API to allow to verify expected behaviour of a
  * sql query under test.
  *
  * @param ec that is required to run mock connections
  * @author Norbert Gogiel
  * @since 0.1
  */
abstract class DoobieHikariTestKit(implicit ec: ExecutionContext) extends HikariTransactorEffect(ec) {

  /** Set expected behaviour of a [[String]] to be expected by mock [[java.sql.PreparedStatement]].
    *
    * @param parameterIndex the index id of a parameter being verified
    * @param string the parameter expected
    */
  def expectSetString(parameterIndex: Int, string: String): CallHandler2[Int, String, Unit] = {
    (mockPreparedStatement.setString(_: Int, _: String)).expects(parameterIndex, string)
  }
}
