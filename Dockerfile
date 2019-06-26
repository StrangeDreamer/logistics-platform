FROM java:8
VOLUME /tmp
ADD target/springboot-seckill-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]