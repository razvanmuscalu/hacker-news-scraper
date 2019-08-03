package com.truelayer.scraper

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.Checkers

trait BaseSpec extends WordSpec with Matchers with ScalaFutures with Checkers
