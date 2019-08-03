package com.truelayer.scraper.scraper
import com.truelayer.scraper.BaseSpec
import com.truelayer.scraper.cli.Input
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.io.Source

class ScraperSpec extends BaseSpec with TableDrivenPropertyChecks {

  private val scraper = new Scraper

  "Scraper.load" should {
    "load an HTML file" in {
      val html = scraper.loadLocalFile("src/test/resources/mocks/hacker-news?p=1")

      html.title shouldBe "Hacker News"
    }

    "load a URL" in {
      val html = scraper.loadURL("https://news.ycombinator.com/")

      html.title shouldBe "Hacker News"
    }

    "load a String" in {
      val path = "src/test/resources/mocks/hacker-news-unit.html"
      val file = Source.fromFile(path).getLines.mkString

      val html = scraper.loadString(file)

      html.title shouldBe "Hacker News"
    }
  }

  "Scraper.scrape" should {
    val path = "src/test/resources/mocks/hacker-news?p=1"

    "extract posts" in {
      val posts = scraper.scrape(path, source = File)

      posts.length shouldBe 5
      posts.map(_.rank) shouldBe List(1, 2, 3, 4, 5)
    }

    "limit posts" in {
      val posts = scraper.scrape(path, Input(2), source = File)

      posts.length shouldBe 2
      posts.map(_.rank) shouldBe List(1, 2)
    }

    "limit posts (case when limit equals the number of posts on first page)" in {
      val posts = scraper.scrape(path, Input(3), source = File)

      posts.length shouldBe 3
      posts.map(_.rank) shouldBe List(1, 2, 3)
    }

    "limit posts (case when having to iterate 3 times)" in {
      val posts = scraper.scrape(path, Input(8), source = File)

      posts.length shouldBe 8
      posts.map(_.rank) shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
    }

    "limit posts (case when not enough items in available pages)" in {
      val posts = scraper.scrape(path, Input(10), source = File)

      posts.length shouldBe 9
      posts.map(_.rank) shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
    }
  }

  "Scraper.extract" should {
    val path = "src/test/resources/mocks/hacker-news-unit.html"
    val file = Source.fromFile(path).getLines.mkString

    // helper method to allow for easy unit-testing of individual element
    def formatFile(title: String = "title",
                   author: String = "author",
                   uri: String = "http://google.com",
                   points: String = "5 points",
                   comments: String = "10 comments",
                   rank: String = "1."): String =
      file
        .replace("${title}", title)
        .replace("${author}", author)
        .replace("${uri}", uri)
        .replace("${points}", points)
        .replace("${comments}", comments)
        .replace("${rank}", rank)

    "extract title" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return title",
          "title",
          List("title")
        ),
        (
          "do not return title if too long",
          "titletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitle",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(title = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.title) shouldBe out
      }
    }

    "extract author" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return author",
          "author",
          List("author")
        ),
        (
          "do not return author if too long",
          "authorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthorauthor",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(author = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.author) shouldBe out
      }
    }

    "extract uri" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return uri",
          "http://google.com",
          List("http://google.com")
        ),
        (
          "do not return uri if not valid",
          "http:// google.com",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(uri = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.uri) shouldBe out
      }
    }

    "extract points" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return points",
          "10 points",
          List(10)
        ),
        (
          "do not return points if not integer",
          "a points",
          List.empty
        ),
        (
          "do not return points if not in expected format (no space)",
          "10points",
          List.empty
        ),
        (
          "do not return points if not in expected format (comma)",
          "10,points",
          List.empty
        ),
        (
          "do not return points if negative integer",
          "-5 points",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(points = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.points) shouldBe out
      }
    }

    "extract comments" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return comments",
          "10 comments",
          List(10)
        ),
        (
          "do not return comments if not integer",
          "a comments",
          List.empty
        ),
        (
          "do not return comments if not in expected format (no space)",
          "10comments",
          List.empty
        ),
        (
          "do not return comments if not in expected format (comma)",
          "10,comments",
          List.empty
        ),
        (
          "do not return comments if negative integer",
          "-5 comments",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(comments = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.comments) shouldBe out
      }
    }

    "extract rank" in {
      val argsExamples = Table(
        (
          "testCase",
          "in",
          "out"
        ),
        (
          "return rank",
          "1.",
          List(1)
        ),
        (
          "do not return rank if not integer",
          "a",
          List.empty
        ),
        (
          "do not return rank if not in expected format (no point at the end for single digit)",
          "1",
          List.empty
        ),
        (
          "do not return rank if not in expected format (no point at the end for multiple digits)",
          "11",
          List.empty
        ),
        (
          "do not return rank if negative integer",
          "-5.",
          List.empty
        ),
      )

      forAll(argsExamples) { (_, in, out) =>
        val formattedFile = formatFile(rank = in)
        val posts = scraper.scrape(formattedFile, Input(1), String)

        posts.map(_.rank) shouldBe out
      }
    }
  }
}
