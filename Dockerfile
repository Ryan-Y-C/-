FROM java:openjdk-8u111-alpine

RUN ["/bin/sh","-c","mkdir ./app"]

WORKDIR /app

COPY target/wechatshop-0.0.1-SNAPSHOT.jar /app

EXPOSE 8080

CMD [ "java", "-jar", "wechatshop-0.0.1-SNAPSHOT.jar" ]
