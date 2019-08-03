package com.truelayer.scraper.cli

import scopt.{OParser, OParserBuilder}

object CliParser {
  private val builder: OParserBuilder[Input] = OParser.builder[Input]
  private val parser1: OParser[Unit, Input] = {
    import builder._

    OParser.sequence(
      programName("scraper"),
      head("scraper", "0.1"),
      // option -p, --posts
      opt[Int]('p', "posts")
        .action((x, c) => c.copy(posts = x))
        .validate(
          x =>
            if (x <= 100) success
            else failure(s"Value $x is larger than 100")
        )
        .text("how many posts to print. A positive integer <= 100.")
    )
  }

  def parseInput(args: Seq[String]): Either[String, Input] =
    OParser.parse(parser1, args, Input()) match {
      case Some(config) => Right(config)
      case _            => Left("Check your input.")
    }
}
