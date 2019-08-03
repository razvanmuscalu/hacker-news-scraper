FROM openjdk:8u121-alpine

ADD scraper-service.jar /usr/local/bin/scraper-service.jar
ADD start.sh /usr/local/bin/start.sh

RUN ["chmod", "+x", "/usr/local/bin/start.sh"]

ENTRYPOINT /usr/local/bin/start.sh