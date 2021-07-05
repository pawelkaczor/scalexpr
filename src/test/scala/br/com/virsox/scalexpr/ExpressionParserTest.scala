package br.com.virsox.scalexpr

import java.time.Instant

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

/** * Tests for expression parsing
  */
class ExpressionParserTest extends AnyFlatSpec with Matchers {

  trait Fixture {
    val parser                 = ExpressionParser()
    val ctx1: Map[String, Any] = Map("value" -> 20.0, "sourceId" -> 1, "name" -> "Test")
    val ctx2: Map[String, Any] = Map("value" -> 7.2, "sourceId" -> 2)
  }

  def verify[T](expr: Try[Expression[T]], result: Expression[T]): Unit = {
    expr match {
      case Success(parsed) => parsed shouldBe result
      case Failure(ex)     => fail(ex)
    }
  }

  "An ExpressionParser" should "parse an arithmetic expression" in new Fixture {
    verify(parser.parseArithmeticExpression("5 +3"), BigDecimalConstant(5) + BigDecimalConstant(3))
  }

  it should "consider operator precedence in an arithmetic expression" in new Fixture {
    verify(parser.parseArithmeticExpression("4+1 * 2"), BigDecimalConstant(4) + (BigDecimalConstant(1) * BigDecimalConstant(2)))
  }

  it should "consider parenthesis precedence in an arithmetic expression" in new Fixture {
    verify(
      parser.parseArithmeticExpression("3 * ((1 + 2) * (5 + 1))"),
      BigDecimalConstant(3) *
        ((BigDecimalConstant(1) + BigDecimalConstant(2)) *
          (BigDecimalConstant(5) + BigDecimalConstant(1)))
    )
  }

  it should "parse variables in an int expression" in new Fixture {
    verify(
      parser.parseArithmeticExpression("sourceId  + 1 * 2"),
      BigDecimalVar("sourceId") + (BigDecimalConstant(1) * BigDecimalConstant(2))
    )
  }

  it should "resolve variables in an int expression with parenthesis" in new Fixture {
    verify(
      parser.parseArithmeticExpression("3 * ((sourceId + 2) * (sourceId + 1))"),
      BigDecimalConstant(3) *
        ((BigDecimalVar("sourceId") + BigDecimalConstant(2)) *
          (BigDecimalVar("sourceId") + BigDecimalConstant(1)))
    )
  }

  it should "calculate an arithmetic expression" in new Fixture {
    verify(parser.parseArithmeticExpression("2 + 30"), BigDecimalConstant(2) + BigDecimalConstant(30))
  }

  it should "resolve variables in an arithmetic expression" in new Fixture {
    verify(parser.parseArithmeticExpression("sourceId + 30"), BigDecimalVar("sourceId") + BigDecimalConstant(30))
  }

  it should "parse an arithmetic expression with doubles" in new Fixture {
    verify(parser.parseArithmeticExpression("7.2 + 3.0"), BigDecimalConstant(7.2) + BigDecimalConstant(3.0))
  }

  it should "consider operator precedence in an arithmetic expression with doubles" in new Fixture {
    verify(
      parser.parseArithmeticExpression("7.2 + 1.8*2.0"),
      BigDecimalConstant(7.2) +
        (BigDecimalConstant(1.8) * BigDecimalConstant(2.0))
    )
  }

  it should "resolve double variables in an arithmetic expression" in new Fixture {
    verify(parser.parseArithmeticExpression("value + 3.0"), BigDecimalVar("value") + BigDecimalConstant(3.0))
  }

  it should "resolve multiple variables in an arithmetic expression with parenthesis" in new Fixture {
    verify(
      parser.parseArithmeticExpression("(value +  3.0 ) *sourceId"),
      (BigDecimalVar("value") + BigDecimalConstant(3.0)) * BigDecimalVar("sourceId")
    )
  }

  it should "parse equality expressions with Strings" in new Fixture {
    verify(parser.parseBooleanExpression("""name == "Wilson""""), StringVar("name") == StringConstant("Wilson"))
    verify(parser.parseBooleanExpression("""name != "John""""), StringVar("name") != StringConstant("John"))
  }

  it should "parse relational expression" in new Fixture {
    verify(
      parser.parseRelationalExpression("""a > 5"""),
      RelationalExpression(BigDecimalVar("a"), GreaterThan, BigDecimalConstant(5))
    )
  }

  it should "parse relational expression with a var containing underscore" in new Fixture {
    verify(
      parser.parseRelationalExpression("""a_var > 5"""),
      RelationalExpression(BigDecimalVar("a_var"), GreaterThan, BigDecimalConstant(5))
    )
  }

  it should "parse comparison expressions with variables on both sides" in new Fixture {
    verify(parser.parseBooleanExpression("""a == b"""), BigDecimalVar("a") == BigDecimalVar("b"))
    verify(parser.parseBooleanExpression("""a != b"""), BigDecimalVar("a") != BigDecimalVar("b"))
    verify(parser.parseBooleanExpression("""a > b"""), BigDecimalVar("a") > BigDecimalVar("b"))
  }

  it should "parse comparison expressions with Ints" in new Fixture {
    verify(parser.parseBooleanExpression("""age == 19"""), BigDecimalVar("age") == BigDecimalConstant(19))
  }

  it should "parse comparison expressions with Longs" in new Fixture {
    verify(parser.parseBooleanExpression("""id == 123456L"""), BigDecimalVar("id") == BigDecimalConstant(123456))
    verify(parser.parseBooleanExpression("""id != 987654L"""), BigDecimalVar("id") != BigDecimalConstant(987654))
  }

  it should "parse comparison expressions with Doubles" in new Fixture {

    verify(parser.parseBooleanExpression("""salary > 10000.00"""), BigDecimalVar("salary") > BigDecimalConstant(10000.0))
    verify(parser.parseBooleanExpression("""average < 22.3"""), BigDecimalVar("average") < BigDecimalConstant(22.3))
  }

  it should "correctly parse comparison expressions with Dates" in new Fixture {

    val date1 = Instant.parse("2015-12-01T10:00:00.000Z")
    val date2 = Instant.parse("2010-12-01T10:00:00.000Z")

    verify(
      parser.parseBooleanExpression("""start == 2015-12-01T10:00:00.000Z"""),
      DateTimeVar("start") == DateTimeConstant(date1)
    )

    verify(parser.parseBooleanExpression("""end   < 2010-12-01T10:00:00.000Z"""), DateTimeVar("end") < DateTimeConstant(date2))
  }

  it should "parse composed boolean expressions" in new Fixture {
    verify(
      parser.parseBooleanExpression("""name == "Wilson" && age == 19"""),
      (StringVar("name") == StringConstant("Wilson")) && (BigDecimalVar("age") == BigDecimalConstant(19))
    )

    verify(
      parser.parseBooleanExpression("""name == "John" || age == 21"""),
      (StringVar("name") == StringConstant("John")) || (BigDecimalVar("age") == BigDecimalConstant(21))
    )
  }

  it should "parse boolean expressions with parenthesis" in new Fixture {
    verify(
      parser.parseBooleanExpression("""name == "Wilson" || (name == "Test" && age == 19)"""),
      (StringVar("name") == StringConstant("Wilson")) ||
        ((StringVar("name") == StringConstant("Test")) && (BigDecimalVar("age") == BigDecimalConstant(19)))
    )
  }

  it should "parse boolean expressions with parenthesis 2" in new Fixture {
    verify(
      parser.parseBooleanExpression("""isFirst == 1 || (name == "Test" && age == 19)"""),
      (BigDecimalVar("isFirst") == BigDecimalConstant(1)) ||
        ((StringVar("name") == StringConstant("Test")) && (BigDecimalVar("age") == BigDecimalConstant(19)))
    )
  }

  it should "parse complex boolean expressions with parenthesis" in new Fixture {
    verify(
      parser.parseBooleanExpression("""(name == "Wilson" && age  > (base + 2.0)) || (name == "Test" && age == ( 4 * 2))"""),
      ((StringVar("name") == StringConstant("Wilson")) &&
        (BigDecimalVar("age") > (BigDecimalVar("base") + BigDecimalConstant(2.0)))) ||
        ((StringVar("name") == StringConstant("Test")) &&
          (BigDecimalVar("age") == (BigDecimalConstant(4) * BigDecimalConstant(2))))
    )
  }
}
