package br.com.virsox.scalexpr

import java.time.Instant

import fastparse._, SingleLineWhitespace._
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

/** Companion object. */
object ExpressionParser {

  /** *
    * Defines a function with a name.
    * @param f Function definition.
    * @param name Name of the function.
    * @tparam T Type of the function parameter.
    * @tparam V Type of the function return type.
    */
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V) {
    def apply(t: T): V              = f(t)
    override def toString(): String = name

  }

  def apply() = new ExpressionParser()
  case class ExpressionParsingException(msg: String) extends Exception
}

/** *
  * Parser of expressions.
  */
class ExpressionParser extends DateParser {

  import ExpressionParser._

  def parseRelationalExpression(str: String): Try[RelationalExpression[_ >: Instant with String with BigDecimal]] =
    parse(str, relationalExpr(_)) match {
      case Parsed.Success(value, _) =>
        Success(value)
      case f: Parsed.Failure =>
        Failure(ExpressionParsingException(s"[${f.index}], ${f.extra.trace()}"))
    }

  /** *
    * Parse a String as a boolean expression.
    * @param str String to be parsed.
    * @return Boolean expression if the String can be successfully parsed, a Failure otherwise.
    */
  def parseBooleanExpression(str: String): Try[Expression[Boolean]] =
    parse(str, booleanExpr(_)) match {
      case Parsed.Success(value, _) =>
        Success(value)
      case f: Parsed.Failure =>
        Failure(ExpressionParsingException(s"[${f.index}], ${f.extra.trace()}"))
    }

  /** *
    * Parse a String as a BigDecimal expression.
    * @param str String to be parsed.
    * @return BigDecimal expression if the String can be successfully parsed, a Failure otherwise.
    */
  def parseArithmeticExpression(str: String): Try[Expression[BigDecimal]] =
    parse(str, arithmeticExpr(_)) match {
      case Parsed.Success(value, _) =>
        Success(value)
      case f: Parsed.Failure =>
        Failure(ExpressionParsingException(s"[${f.index}], ${f.extra.trace()}"))
    }

  // ---------------------------------------------------------------
  // -----------------    General parsers     ----------------------
  // ---------------------------------------------------------------
  def chars[_: P]: P[Unit]  = P(CharIn("a-zA-Z_"))
  def digits[_: P]: P[Unit] = P(CharIn("0-9"))

  // ---------------------------------------------------------------
  // -----------------    Number parsers      ----------------------
  // ---------------------------------------------------------------

  // a decimal is 0 or [+-] followed by [1-9] followed by digits
  def decimalParser[_: P]: P[Unit] = P("0" | CharIn("+\\-").? ~ CharIn("1-9") ~ digits.rep)

  // an int is a decimal number that does not end with a L
  def intParser[_: P]: P[String] = P(decimalParser ~ !CharIn("Ll.")).!

  // a long is a decimal number that ends with a L character
  def longParser[_: P]: P[String] = P(decimalParser.! ~ CharIn("Ll").!).map(_._1)

  // a double is a fractional number
  def doubleParser[_: P]: P[String] = P(decimalParser ~ "." ~ digits.rep).!

  // convert the parsed numbers to constant objects
  def intLiteral[_: P]: P[NumericExpr[Int]]       = intParser.map(s => IntConstant(s.toInt))
  def longLiteral[_: P]: P[NumericExpr[Long]]     = longParser.map(s => LongConstant(s.toLong))
  def doubleLiteral[_: P]: P[NumericExpr[Double]] = doubleParser.map(s => DoubleConstant(s.toDouble))
  def bigDecimalLiteral[_: P]: P[NumericExpr[BigDecimal]] =
    (intParser | longParser | doubleParser).map(s => BigDecimalConstant(BigDecimal(s)))

  // ---------------------------------------------------------------
  // -----------------    Variable parsers    ----------------------
  // ---------------------------------------------------------------

  // a valid identifier is composed of a character, followed by n characters or digits
  def identifierParser[_: P]: P[Unit] = P(chars ~ (chars | digits).rep)

  // a variable is a sequence of identifiers separated by "."
  def variableParser[_: P]: P[Unit] = P(identifierParser ~ ("." ~ identifierParser).rep)

  def intVariable[_: P]: P[IntVar]               = variableParser.!.map(IntVar)
  def longVariable[_: P]: P[LongVar]             = variableParser.!.map(LongVar)
  def doubleVariable[_: P]: P[DoubleVar]         = variableParser.!.map(DoubleVar)
  def bigDecimalVariable[_: P]: P[BigDecimalVar] = variableParser.!.map(BigDecimalVar)
  def dateTimeVariable[_: P]: P[DateTimeVar]     = variableParser.!.map(DateTimeVar)
  def booleanVariable[_: P]: P[BooleanVar]       = variableParser.!.map(BooleanVar)
  def stringVariable[_: P]: P[StringVar]         = variableParser.!.map(StringVar)

  // ---------------------------------------------------------------
  // ------------------   String parsers    ------------------------
  // ---------------------------------------------------------------
  def StringChars[_: P]: NamedFunction[Char, Boolean] = NamedFunction(!"\"\\".contains(_: Char), "StringChars")
  def strChars[_: P]: P[Unit]                         = P(CharsWhile(StringChars))
  def stringLiteral[_: P]: P[StringConstant]          = P("\"" ~ strChars.! ~ "\"").map(StringConstant)

  // ---------------------------------------------------------------
  // -----------------   Numeric expressions   ---------------------
  // ---------------------------------------------------------------
  /** *
    * Obtains a numeric variable parser based on the informed type.
    * @tparam T Type of the variable returned by the parser.
    * @return numeric variable parser.
    */
  def numericVariable[_: P, T: TypeTag]: P[NumericExpr[T]] =
    implicitly[TypeTag[T]].tpe match {
      case _ if typeOf[T] =:= typeOf[Int]        => intVariable.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[Long]       => longVariable.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[Double]     => doubleVariable.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[BigDecimal] => bigDecimalVariable.asInstanceOf[P[NumericExpr[T]]]
    }

  /** *
    * Obtains a numeric literal parser based on the informed type.
    * @tparam T Type of the literal returned by the parser.
    * @return numeric literal parser.
    */
  def numericLiteral[_: P, T: TypeTag]: P[NumericExpr[T]] =
    implicitly[TypeTag[T]].tpe match {
      case _ if typeOf[T] =:= typeOf[Int]        => intLiteral.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[Long]       => longLiteral.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[Double]     => doubleLiteral.asInstanceOf[P[NumericExpr[T]]]
      case _ if typeOf[T] =:= typeOf[BigDecimal] => bigDecimalLiteral.asInstanceOf[P[NumericExpr[T]]]
    }

  /** *
    * Builds a ArithmeticExpression composed of a left-hand subexpression followed by a sequence of
    * operations performed on this subexpression.
    *
    * @param tree Tuple containing the left-hand subexpression and the sequence of operations.
    * @tparam T Type of the numeric expression.
    * @return ArithmeticExpression representing the operations.
    */
  def evalNumeric[T: TypeTag](tree: (NumericExpr[T], Seq[(String, NumericExpr[T])])): NumericExpr[T] = {
    val (base, ops) = tree
    ops.foldLeft(base) {
      case (left, (op, right)) =>
        op match {
          case "+" => left + right
          case "-" => left - right
          case "*" => left * right
          case "/" => left / right
        }
    }
  }

  /** Parser of a numeric expression with parenthesis. */
  def parens[A: P, T: TypeTag]: P[NumericExpr[T]] = P("(" ~ numExpr[A, T] ~ ")")

  /** Parser of multiplication / division factors. */
  def factor[A: P, T: TypeTag]: P[NumericExpr[T]] = P(numericLiteral[A, T] | numericVariable[A, T] | parens)

  /** Parser for division or multiplication expressions. */
  def divMul[A: P, T: TypeTag]: P[NumericExpr[T]] = P(factor[A, T] ~ (CharIn("*/").! ~ factor[A, T]).rep).map(evalNumeric[T])

  /** Parser for arithmetic expressions. */
  def arithmeticExpr[A: P]: P[NumericExpr[BigDecimal]] = numExpr[A, BigDecimal]

  def numExpr[A: P, T: TypeTag]: P[NumericExpr[T]] = P(divMul[A, T] ~ (CharIn("+\\-").! ~ divMul[A, T]).rep).map(evalNumeric[T])

  // ---------------------------------------------------------------
  // -----------------   Relational expressions   ------------------
  // ---------------------------------------------------------------

  /** *
    * Builds a RelationalExpression composed of a left-hand subexpression followed by a sequence of
    * operations performed on this subexpression.
    *
    * @param tree Tuple containing the left-hand subexpression and the sequence of operations.
    * @tparam T Type of the numeric expression.
    * @return RelationalExpression representing the operations.
    */
  def evalRelational[T: Ordering](tree: (OrderingExpr[T], String, OrderingExpr[T])): RelationalExpression[T] = {
    val (left, op, right) = tree
    op match {
      case "==" => left == right
      case "!=" => left != right
      case ">=" => left >= right
      case "<=" => left <= right
      case ">"  => left > right
      case "<"  => left < right
    }
  }

  def dateTimeTerm[_: P]: P[OrderingExpr[Instant] with SimpleExpression[Instant]] = P(dateTimeVariable | dateTimeLiteral)

  def relationalOperators[_: P]: P[Unit] = P(StringIn("==", "!=", "<=", ">=", "<", ">"))

  def strRelationalExpr[A: P]: P[RelationalExpression[String]] =
    P((stringLiteral ~ relationalOperators.! ~ stringVariable) | (stringVariable ~ relationalOperators.! ~ stringLiteral))
      .map(evalRelational[String])

  def numericRelationalExpr[A: P]: P[RelationalExpression[BigDecimal]] =
    P(numExpr[A, BigDecimal] ~ relationalOperators.! ~ numExpr[A, BigDecimal]).map(evalRelational[BigDecimal])

  def dateTimeRelationalExpr[A: P]: P[RelationalExpression[Instant]] =
    P((dateTimeLiteral ~ relationalOperators.! ~ dateTimeVariable) | (dateTimeVariable ~ relationalOperators.! ~ dateTimeLiteral))
      .map(evalRelational[Instant])

  def relationalExpr[_: P]: P[RelationalExpression[_ >: Instant with String with BigDecimal]] =
    P(strRelationalExpr | dateTimeRelationalExpr | numericRelationalExpr)

  // ---------------------------------------------------------------
  // ------------------   Boolean expressions   --------------------
  // ---------------------------------------------------------------

  def booleanLiteral[_: P]: P[BooleanConstant] = P("true" | "false").!.map(s => BooleanConstant(s.toBoolean))
  def booleanParens[_: P]: P[BooleanExpr]      = P("(" ~/ booleanExpr ~ ")")

  def booleanTerm[_: P]: P[BooleanExpr] = P(booleanParens | booleanLiteral | relationalExpr)

  def booleanOperators[_: P]: P[Unit] = P(StringIn("&&", "||"))
  def booleanExpr[_: P]: P[BooleanExpr] =
    P(booleanTerm ~ (booleanOperators.! ~ booleanTerm).rep).map(s => {
      val (base, ops) = s
      ops.foldLeft(base) {
        case (left, (op, right)) =>
          op match {
            case "&&" => left && right
            case "||" => left || right
          }
      }
    })

}
