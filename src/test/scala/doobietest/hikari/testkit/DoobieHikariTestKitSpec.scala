package doobietest.hikari.testkit

import cats.effect.{ IO, Resource }
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.hikari.testkit.DoobieHikariTestKit
import doobie.implicits._
import doobie.util.transactor.{ Interpreter, Strategy, Transactor }
import org.scalamock.handlers.CallHandler0
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.{ a, convertToAnyMustWrapper }
import org.scalatest.wordspec.AnyWordSpecLike

import java.sql.{ Connection, PreparedStatement }
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepoKit extends DoobieHikariTestKit

class UserRepository(transactor: Resource[IO, HikariTransactor[IO]]) {
  def persistUser(username: String, password: String): IO[Int] = {
    transactor.use { xa =>
      sql"""insert into user (username, password) values ($username, $password)""".stripMargin.update.run
        .transact(xa)
    }
  }
}

class DoobieHikariTestKitSpec extends AnyWordSpecLike with MockFactory {

  "DoobieHikariTestKit underlying mechanism" should {
    val testDAO: UserRepoKit = new UserRepoKit

    "instantiate a mock Connection" in {
      (() => testDAO.mockConnection.createStatement(_: Int, _: Int)).expects() mustBe a[CallHandler0[Connection]]
    }

    "instantiate a mock PreparedStatement" in {
      (() => testDAO.mockPreparedStatement.execute(_: String)).expects() mustBe a[CallHandler0[PreparedStatement]]
    }

    "instantiate a valid ContextShift" in {
      testDAO.ioContextShift.shift mustBe a[IO[Unit]]
    }

    "instantiate a valid mock of HikariDataSource" in {
      (() => testDAO.mockHikariDataSource.getConnection(_: String, _: String))
        .expects() mustBe a[CallHandler0[HikariDataSource]]
    }

    "instantiate a valid Transactor" in {
      testDAO.transactor mustBe a[Transactor.Aux[IO, HikariDataSource]]
    }

    "instantiate Transactor with HikariDataSource" in {
      testDAO.transactor.kernel mustBe a[HikariDataSource]
    }

    "instantiate Transactor with Interpreter" in {
      testDAO.transactor.interpret mustBe a[Interpreter[IO]]
    }

    "instantiate Transactor with void Strategy" in {
      testDAO.transactor.strategy mustBe Strategy.void
    }

    "instantiate mock Resource Transactor" in {
      testDAO.mockTransactor mustBe a[Resource[IO, HikariTransactor[IO]]]
      testDAO.resourceRelease mustBe a[HikariTransactor[IO] => IO[Unit]]
      testDAO.resourceRelease.apply(testDAO.transactor) mustBe a[IO[Unit]]
    }

    "instantiate Transactor with Connect" in {
      testDAO.transactor.connect.apply(testDAO.mockHikariDataSource) mustBe a[Resource[IO, Connection]]
    }
  }

  "DoobieHikariTestKit API" should {
    val testDAO: UserRepoKit = new UserRepoKit
    val testRepository: UserRepository = new UserRepository(testDAO.mockTransactor)

    "allow to mock a string entry" in {
      testRepository.persistUser("testString", "testStringTwo")

      testDAO.expectSetString(1, "testString")
    }

    "allow to mock a numerous indexed string parameter" in {
      testRepository.persistUser("testString", "testStringTwo")

      testDAO.expectSetString(2, "testStringTwo")
    }
  }
}
