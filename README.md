# hacker-news-scraper

A simple command line application that reads Hacker News and outputs to STDOUT the top posts in JSON.

## package and run

The project is a Scala service managed through Maven.

`mvn clean package` will clean, run the tests and generate an executable JAR under `target/docker-src/scraper-service.jar`

`mvn org.antipathy:mvn-scalafmt_2.12:0.7_1.5.1:format` formats the code as per the `.scalafmt.conf` rules. This might be required to be run before `mvn clean package` if the code is not properly formatted.

I added a `scraper-service.jar` file in the root directory so you can use this one directly.

Run `java -jar scraper-service.jar --posts <number of posts>` in order to execute the application.

## libraries

This is actually the first time I had to write a scraper service. I decided to do it in Scala because it's the language I used most often in the past one year and half. I had fun writing this code!

I used `scala-scraper` as the HTML scraping engine, which is a Scala wrapper around `JSoup`. It's pretty much the only scraping library in Scala so I had to use it. However, being the first time I used this library, I found it quite easy to understand and use. So I would recommend this library.

I used `scopt` in order to handle command line input arguments. I heard of this library from a colleague and wanted to use it for a while. I have to say it's a pretty good library, again easy to use, which does pretty much everything cli-related for you.

I used `circe` as the JSON encoding engine. This is a pretty known and heavily used library in the Scala community.

## docker

I added a `start.sh` and a `Dockerfile` under `src/main/resources/docker`. I haven't had the time to test this however. It's a pretty basic `Dockerfile` template I used a few years ago in a previous team when I was working with Java. With Scala, usually this is handled natively by SBT (a package manager for Scala). 