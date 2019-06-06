**基于SpringBoot实现Java高并发之反向拍卖物流抢单系统**

**技术栈**

* 后端(localhost:8080)： SpringBoot-2.x + Redis-4.x+Mysql(docker容器)

* 前端(用于测试)： Bootstrap + Jquery


**测试环境**

* IDEA + Maven-10.13 + Tomcat8 + JDK8

**启动说明**
* 如果不想安装mysql和rabbitmq组件，请在启动前安装其容器，命令行如下（前提安装docker和docker-compose）
`docker-compose -f docker/mysql.yml up -d`
`docker-compose -f docker/rabbitmq.yml up -d`
* adminer(localhost:8000)是web端的mysql管理工具，用户名/密码是root/1234，
* 如果已经安装相关组件，请配置好 [application.yml](https://github.com/TyCoding/springboot-seckill/blob/master/src/main/resources/application.yml) 中连接数据库的用户名和密码，以及Redis服务器的地址和端口信息。同时，请创建数据库`seckill`，建表SQL语句放在：db。

**[OpenAPI](https://documenter.getpostman.com/view/5434571/S1TU1dCy?version=latest)**

**项目设计**


* docker使用
`docker kill dockerName`
`dockcer rm dockerName`
* docker容器重启之后MySQL里面的数据将全部清空
* mac上的mysql的hostname是192.168.99.100,所以在Windows上开发时需要改回localhost

```
├─db
│      script.sql
│
├─docker
│      mysql.yml
│      rabbitmq.yml
│      redis.yml
│
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─tycoding
│  │  │          │  SpringbootSeckillApplication.java
│  │  │          │
│  │  │          ├─domain
│  │  │          │      BankAccount.java
│  │  │          │      Bid.java
│  │  │          │      Cargo.java
│  │  │          │      CargoOrder.java
│  │  │          │      CargoOrderLite.java
│  │  │          │      GuarantorAccount.java
│  │  │          │      Receiver.java
│  │  │          │      Seckill.java
│  │  │          │      SeckillOrder.java
│  │  │          │      Shipper.java
│  │  │          │      Truck.java
│  │  │          │
│  │  │          ├─dto
│  │  │          │      CargoInfoChangeDTO.java
│  │  │          │      Exposer.java
│  │  │          │      SeckillExecution.java
│  │  │          │      SeckillResult.java
│  │  │          │
│  │  │          ├─enums
│  │  │          │      SeckillStatEnum.java
│  │  │          │
│  │  │          ├─exception
│  │  │          │      CargoException.java
│  │  │          │      CargoOrderException.java
│  │  │          │      RepeatKillException.java
│  │  │          │      SeckillCloseException.java
│  │  │          │      SeckillException.java
│  │  │          │
│  │  │          ├─rabbitmq
│  │  │          │      rabbitMQConfig.java
│  │  │          │      RabbitTopic.java
│  │  │          │
│  │  │          ├─redis
│  │  │          │      JedisConfig.java
│  │  │          │      RedisTemplateConfig.java
│  │  │          │
│  │  │          ├─repository
│  │  │          │      CargoRepository.java
│  │  │          │      ReceiverMapper.java
│  │  │          │      ReceiverRepository.java
│  │  │          │      RecieverMapper.java
│  │  │          │      SeckillMapper.java
│  │  │          │      SeckillOrderMapper.java
│  │  │          │      ShipperMapper.java
│  │  │          │      ShipperRepository.java
│  │  │          │      TruckRepository.java
│  │  │          │
│  │  │          ├─resource
│  │  │          │      BaseController.java
│  │  │          │      CargoOrderResource.java
│  │  │          │      CargoResource.java
│  │  │          │      SeckillController.java
│  │  │          │      SendMsg.java
│  │  │          │      ShipperResource.java
│  │  │          │
│  │  │          ├─scheduledTasks
│  │  │          │      ScheduleConfig.java
│  │  │          │      ScheduledTasks1.java
│  │  │          │
│  │  │          ├─service
│  │  │          │  │  CargoService.java
│  │  │          │  │  ReceiverService.java
│  │  │          │  │  SeckillService.java
│  │  │          │  │  ShipperService.java
│  │  │          │  │  TruckService.java
│  │  │          │  │
│  │  │          │  └─impl
│  │  │          │          SeckillServiceImpl.java
│  │  │          │
│  │  │          └─websocket
│  │  │                  MyWebSocket.java
│  │  │                  WebSocketConfig.java
│  │  │                  WebSocketServer.java
│  │  │                  WebSocketTest.java
│  │  │
│  │  └─resources
│  │      │  application.yml
│  │      │
```

- baseUrl= localhost:8080

- 抢单包含如下API
   - Sender创建POST了一个订单，同时将所有订单放进Redis中（或者队列中）
     - POST /sender/cargo/create
   - 承运方Trunk查询所有可以抢的订单
     - GET /truck/cargos/list
   - 承运方Trunk发送抢单价格，返回抢单是否成功
     - POST /trunk/{price}  

- 承运方Trunk的转单API
   - Trunk更新UPDATE了一个订单，同时将所有订单放进Redis中（或者队列中）
     - PUT /trunk/{cargoId} 
   - 承运方Trunk查询所有可以抢的订单
     - GET /trunk/cargos
   - 承运方Trunk发送抢单价格，返回抢单是否成功
     - POST /trunk/{price}  

- 订单完成（给订单状态属性赋值——完成、失败【超时、验货不通过】）
  - Reciever更新Update订单状态
    - PUT /reciever/{cargoId}
  - 平台将该运费支付给承运方 （有个属性paidMoney）
    - PUT /cargo/{cargoId}  更新paidMoney属性值

- 订单超时

  - 承运方trunk请求订单要送达接口，获取当前时间与订单被抢单的时间相减，差值与订单规定的运送时间对比。返回是否超时以及超时时长

  - Trunk承运方获取超时计费额度
  
  
  - 断线Notification：Push，Notification，Service Work

- [ ] 将订单结果返回给所有请求过的用户，可能考虑使用rabbitmq
- [ ] 将所有竞价信息缓存下来（需要将所有的请求时间保存），时间一到请求就将history刷进数据库。
- [ ] 订单状态判断（超时--当平台发送停止抢单时，需要查看当前是否有人抢单，没有就将状态置为超时，完成--有人抢，将状态置为完成，转单--当truck更新订单就将订单的状态更新为转单）
- [ ] Swagger 引入

- [x] 各类domain定义，以及与数据库的映射
- [x] 4方（shipper，平台方，receiver，truck）的openAPI设计

- [x] 承运方竞价抢单
- [x] 数据库记录抢单结果