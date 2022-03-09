package doobie.hikari.testkit

import org.scalamock.scalatest.MockFactory

import java.sql.{ Connection, PreparedStatement }

/**  Mocks out Java SQL resources to enable database operations with no actual resource.
  *
  *  @author Norbert Gogiel
  *  @since 0.1
  */
private[doobie] trait TestJavaSqlConnection extends MockFactory {
  val mockConnection: Connection = mock[Connection]
  val mockPreparedStatement: PreparedStatement = mock[PreparedStatement]
  (mockConnection.prepareStatement(_: String)).expects(*).returns(mockPreparedStatement)
}
