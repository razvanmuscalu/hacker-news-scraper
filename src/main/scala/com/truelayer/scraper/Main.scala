package com.truelayer.scraper
import com.truelayer.scraper.cli.CliParser
import com.truelayer.scraper.scraper.Scraper
import io.circe.generic.auto._
import io.circe.syntax._

object Main extends App {

  private val scraper = new Scraper
  private val url = "https://news.ycombinator.com/news?p=1"

  CliParser.parseInput(args) match {
    case Right(config) => scraper.scrape(url, config).foreach(i => println(i.asJson))
    case Left(msg)     => println(msg)
  }
}
