- mysql与redis均为docker容器
```$xslt
docker pull redis
docker run -p 6379:6379 --name l-redis -d redis
docker pull mysql
docker run -p 3306:3306 --name l-mysql -e MYSQL_ROOT_PASSWORD=root -d mysql
docker ps
```
- 进入mysql容器，并创建seckill数据库
```$xslt
docker exec -it l-mysql bash
mysql -u root -p
root
create database seckill;

```
- 进入redis容器相关操作
```$xslt
docker exec -it l-redis bash
redis-cli -h 127.0.0.1 -p 6379
flushAll
```
- maven打包项目
```$xslt
mvn clean package
java -jar target/spring......
```