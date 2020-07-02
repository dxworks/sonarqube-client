FROM openjdk:8

RUN mkdir /usr/sonarclient

ADD bin/sonarclient.sh /usr/sonarclient/sonarclient.sh
ADD target/sonarclient.jar /usr/sonarclient/sonarclient.jar

WORKDIR /usr/sonarclient
RUN chmod +x sonarclient.sh

CMD ["bash", "./sonarclient.sh"]
