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
* 统一使用我服务器上的MySQL服务，不用改

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
│  │  │          │      Bid.java
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
   - Shipper创建【POST】了一个订单
   - 平台确定该订单开抢，同时讲该订单放进Redis中,如果订单状态改变
     - POST /sender/cargo/create
   - 承运方Trunk查询所有可以抢的订单，只向truck暴露可以抢的订单，因此在查询的时候应该是根据订单状态来的
     - GET /truck/cargos/list
   - 承运方Trunk发送抢单价格，返回排队是否成功
     - POST /trunk/{price}  
   - 时间截止，平台发送截止抢单请求，生成最终抢单结果，返回给前台，前台通知相应的truck是否成功接单

- 承运方Trunk的转单API
   - Trunk新建【POST】了一个订单，该订单记录着原始订单号以及原始订单的发货方出价，不管转单几次，这2个字段均是原始订单的信息，不是前一个订单的信息。
   ，接着走创建订单的步骤
     - PUT /trunk/{cargoId} 
   - 承运方Trunk查询所有可以抢的订单
     - GET /trunk/cargos
   - ......

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

- [ ] 资金结算全过程

- [ ] 三种撤单方式

- [ ] 平台参数的初始值设置

- [x] 平台当前状态的展示（包含当前注册车辆、发货方数量、正在执行的订单数量、资金等）;平台参数查看

- [x] 平台参数的设置

- [x] 获取某辆车当前的当前已接未运货物数量和正在运送货物数量

- [x]  按照条件查询订单：承运方id、发货方id、收货方id、货物状态（目前的需求包括异常订单、超时订单、正常完成的订单、正在发布的订单）

- [x]  3 已经在运  --truck发送请求说明开始运该单

- [x]  4 已运达等待收货  --truck发送已经运达请求，要求验货

- [x]  8 9 10 收货方进行验货

- [x] 如果将所有的竞价请求保存下来可能会降低并发量，请求时就将竞价请求存入mysql，时间一到将Cargo的trunkId和BidPrice更新

- [x] 订单状态判断

- [x] 抢单并通知改承运方抢单成功

- [x] 出价有效性判断

- [x] 转单、撤单

- [x] 各类domain定义，以及与数据库的映射

- [x] 4方（shipper，平台方，receiver，truck）的openAPI设计

- [x] 承运方竞价抢单

- [x] 数据库记录抢单结果




**货物状态**

     * 0 订单创建未发布 --创建订单
     *
     * 1 订单发布中 --平台确定开抢
     *
     * 2 已接未运 --平台确定最终truck抢单成功
     *
     * 3 已经在运
     *
     * 4 已运达等待收货
     *
     * 5 转手中（正挂在平台公告栏上）--转单成功
     *
     * 6 发布时无人接单撤单 --撤单
     *
     * 7 已接未运撤单 --撤单
     *
     * 8 订单完结 --确定收单时
     *
     * 9 订单超时 --确定收单时
     *
     * 10 订单异常 --确定收单时
     *
     * 11 被正常转单，本订单已经失效
