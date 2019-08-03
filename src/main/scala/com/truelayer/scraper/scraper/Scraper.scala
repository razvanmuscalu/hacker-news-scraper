package com.truelayer.scraper.scraper

import java.net.URL

import com.truelayer.scraper.cli.Input
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

import scala.annotation.tailrec
import scala.util.Try

class Scraper {

  val browser = JsoupBrowser()

  def loadLocalFile(path: String): browser.DocumentType = browser.parseFile(path)

  def loadURL(url: String): browser.DocumentType = browser.get(url)

  def loadString(html: String): browser.DocumentType = browser.parseString(html)

  def scrape(path: String, input: Input = Input(), source: HtmlSource = URL): List[Post] =
    scrapeAccumulator(path, List.empty, input, source)

  // helper method that tail-recursively accumulates posts on multiple pages
  @tailrec
  private def scrapeAccumulator(path: String, acc: List[Post], input: Input, source: HtmlSource): List[Post] =
    acc match {
      // reached required number of posts
      case x if x.length >= input.posts => x.take(input.posts)
      // look for more posts in next page
      case x =>
        val doc = source match {
          case URL    => loadURL(path)
          case File   => loadLocalFile(path)
          case String => loadString(path)
        }

        val items = extractPosts(doc)
        val newItems = x ++ items

        val basePath = path.split("\\?").head

        doc >?> attr("href")(".morelink") match {
          // there actually exists a next page
          case Some(s) =>
            val newPath = s.split("\\?")(1)
            val newDocPath = basePath + "?" + newPath
            scrapeAccumulator(newDocPath, newItems, input, source)
          // there is no next page => returns what has been accumulated so far
          case _ => newItems.take(input.posts)
        }
    }

  private def extractPosts(doc: browser.DocumentType): List[Post] = {
    val things = doc >> elementList(".itemlist tbody .athing")
    val subTexts = doc >> elementList(".itemlist tbody .subtext")

    things
      .zip(subTexts)
      .flatMap {
        case (athing, subtext) =>
          for {
            title <- extractTitle(athing)
            uri <- extractUri(athing)
            author <- extractAuthor(subtext)
            points <- extractPoints(subtext)
            comments <- extractComments(subtext)
            rank <- extractRank(athing)
          } yield Post(title, uri, author, points, comments, rank)
      }
  }

  private def extractTitle(elem: Element): Option[String] =
    (elem >/~ validator(text(".storylink"))(_.length < 256) >> text(".storylink")).toOption

  private def extractUri(elem: Element): Option[String] = {
    val uri = validator(attr("href")(".title a"))(i => Try(new URL(i).toURI).isSuccess)
    (elem >/~ uri >> attr("href")(".title a")).toOption
  }

  private def extractAuthor(elem: Element): Option[String] =
    (elem >/~ validator(text(".hnuser"))(_.length < 256) >> text(".hnuser")).toOption

  private def extractPoints(elem: Element): Option[Int] = {
    val points = (elem >?> text(".score")).map(_.split(" ").head)
    toPositiveInt(points)
  }

  private def extractComments(elem: Element): Option[Int] = {
    val comments = (elem >?> text("td:nth-child(2) a:nth-child(6)")).map(_.split(" ").head)
    toPositiveInt(comments)
  }

  private def extractRank(elem: Element): Option[Int] = {
    val rank = (elem >/~ validator(text(".rank"))(_.endsWith(".")) >> text(".rank")).toOption.map(_.dropRight(1))
    toPositiveInt(rank)
  }

  private def toPositiveInt(s: Option[String]): Option[Int] =
    Try(s.map(_.toInt)).toOption.flatten match {
      case Some(i) if i > 0 => Some(i)
      case _                => None
    }
}

sealed trait HtmlSource

case object File extends HtmlSource
case object String extends HtmlSource
case object URL extends HtmlSource
