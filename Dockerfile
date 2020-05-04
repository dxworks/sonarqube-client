FROM openjdk:8

RUN mkdir /usr/sonarclient

ARG JAR_FILE

ADD bin/sonarclient.sh /usr/sonarclient/sonarclient.sh
ADD target/${JAR_FILE} /usr/sonarclient/sonarclient.jar

WORKDIR /usr/sonarclient
RUN chmod +x sonarclient.sh
