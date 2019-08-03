package com.truelayer.scraper.cli
import com.truelayer.scraper.BaseSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class CliParserSpec extends BaseSpec with TableDrivenPropertyChecks {

  private val argsExamples = Table(
    (
      "testCase",
      "args",
      "out"
    ),
    (
      "return error when no arg provided",
      Array(""),
      Left("Check your input.")
    ),
    (
      "return error when arg is not int",
      Array("--posts", "a"),
      Left("Check your input.")
    ),
    (
      "return error when arg name is not specified correctly",
      Array("--blah", "10"),
      Left("Check your input.")
    ),
    (
      "return error when arg name is not specified correctly",
      Array("-b", "10"),
      Left("Check your input.")
    ),
    (
      "return error when arg is too big",
      Array("--posts", "10"),
      Right(Input(10))
    ),
    (
      "return parsed input",
      Array("--posts", "101"),
      Left("Check your input.")
    ),
  )

  "CliParser" should {
    "parse input args" in {
      forAll(argsExamples) { (_, args, out) =>
        CliParser.parseInput(args) shouldBe out
      }
    }
  }
}
