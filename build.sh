#!/bin/bash
git pull
mvn clean package -Dmaven.test.skip=true
# docker build -t logistics:v1.0 .
# docker run --name logistics -p 8080:8080 -d -v /opt/jar/springBootDocker/logs:/log logistics:v1.0 /bin/bash
nohup java -jar target/springboot-seckill-0.0.1-SNAPSHOT.jar >nohup.out &

