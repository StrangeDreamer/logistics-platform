**基于SpringBoot实现Java高并发之反向拍卖物流抢单系统**

**技术栈**

* 后端： SpringBoot-2.x + Redis-4.x+Mysql

* 前端**(用于测试）**： Bootstrap + Jquery

**测试环境**

* IDEA + Maven-10.13 + Tomcat8 + JDK8

**启动说明**

* 启动前，请配置好 [application.yml](https://github.com/TyCoding/springboot-seckill/blob/master/src/main/resources/application.yml) 中连接数据库的用户名和密码，以及Redis服务器的地址和端口信息。

* 启动前，请创建数据库`seckill`，建表SQL语句放在：db。**（不完善）还需要进一步根据遗留代码提炼出entity**



**项目设计**

```

│
├─db   数据库脚本
├─src
│  ├─main
│  │  ├─java
│  │  │  └─cn
│  │  │      └─tycoding
│  │  │          │  SpringbootSeckillApplication.java 启动类
│  │  │          │
│  │  │          ├─domain  
│  │  │          │      Cargo.java
│  │  │          │      CargoOrder.java
│  │  │          │      Reciever.java
│  │  │          │      Seckill.java
│  │  │          │      SeckillOrder.java
│  │  │          │      Shipper.java
│  │  │          │      Trunk.java
│  │  │          │
│  │  │          ├─dto  暂时不管
│  │  │          │      Exposer.java
│  │  │          │      SeckillExecution.java
│  │  │          │      SeckillResult.java
│  │  │          │
│  │  │          ├─enums 暂时不管
│  │  │          │      SeckillStatEnum.java
│  │  │          │
│  │  │          ├─exception 暂时不管
│  │  │          │      RepeatKillException.java
│  │  │          │      SeckillCloseException.java
│  │  │          │      SeckillException.java
│  │  │          │
│  │  │          ├─redis 暂时不管
│  │  │          │      JedisConfig.java
│  │  │          │      RedisTemplateConfig.java
│  │  │          │
│  │  │          ├─repository 操作entity的抽象接口，统一以**Repository命名文件
│  │  │          │      CargoRepository.java
│  │  │          │      RecieverMapper.java
│  │  │          │      SeckillMapper.java
│  │  │          │      SeckillOrderMapper.java
│  │  │          │      ShipperMapper.java
│  │  │          │      ShipperRepository.java
│  │  │          │
│  │  │          ├─resource 向外提供API服务类，统一以**Resource命名文件
│  │  │          │      BaseController.java
│  │  │          │      CargoResource.java
│  │  │          │      pushWeb.java
│  │  │          │      SeckillController.java
│  │  │          │      ShipperResource.java
│  │  │          │
│  │  │          ├─service 操作entity的抽象接口实现类
│  │  │          │  │  CargoService.java
│  │  │          │  │  SeckillService.java
│  │  │          │  │  ShipperService.java
│  │  │          │  │
│  │  │          │  └─impl 暂时不管
│  │  │          │          SeckillServiceImpl.java
│  │  │          │
│  │  │          └─websocket websocket配置类
│  │  │                  WebSocketConfig.java
│  │  │                  WebSocketServer.java
│  │  │
│  │  └─resources
│  │      │  application.yml 项目配置文件
│  │      │
│  │      ├─mapper 暂时不管
│  │      │      SeckillMapper.xml
│  │      │      SeckillOrderMapper.xml
│  │      │
│  │      ├─static 暂时不管
│  │      │  ├─css
│  │      │  │      public.css
│  │      │  │      seckill.css
│  │      │  │      seckill_item.css
│  │      │  │
│  │      │  ├─js
│  │      │  │      seckill_detail.js
│  │      │  │
│  │      │  └─lib
│  │      │      │  bootstrap.min.css
│  │      │      │  bootstrap.min.js
│  │      │      │  countdown.js
│  │      │      │  jquery-3.3.1.min.js
│  │      │      │  jquery.cookie.js
│  │      │      │
│  │      │      └─font
│  │      │          ├─css
│  │      │          │      font-awesome.min.css
│  │      │          │
│  │      │          ├─fonts
│  │      │          │      fontawesome-webfont.eot
│  │      │          │      fontawesome-webfont.svg
│  │      │          │      fontawesome-webfont.ttf
│  │      │          │      fontawesome-webfont.woff
│  │      │          │      fontawesome-webfont.woff2
│  │      │          │      FontAwesome.otf
│  │      │          │      glyphicons-halflings-regular.eot
│  │      │          │      glyphicons-halflings-regular.svg
│  │      │          │      glyphicons-halflings-regular.ttf
│  │      │          │      glyphicons-halflings-regular.woff
│  │      │          │      glyphicons-halflings-regular.woff2
│  │      │          │
│  │      │          └─icon
│  │      │                  clock.png
│  │      │                  seckillbg.png
│  │      │
│  │      ├─swagger
│  │      │      api-v0.0.1.yaml
│  │      │
│  │      └─templates
│  │          ├─page
│  │          │      seckill.html
│  │          │      seckill_detail.html
│  │          │
│  │          ├─public
│  │          │      footer.html
│  │          │      header.html
│  │          │
│  │          └─static
│  │                  index.html
│  │                  index2.html
│  │
│  └─test 暂时不管
│      └─java
│          └─cn
│              └─tycoding
│                  │  SpringbootSeckillApplicationTests.java
│                  │
│                  ├─LogisticsPlatform
│                  ├─redis
│                  │      RedisTemplateConfigTest.java
│                  │
│                  ├─repository
│                  │      SeckillMapperTest.java
│                  │      SeckillOrderMapperTest.java
│                  │
│                  └─service
│                      └─impl
│                              SeckillServiceImplTest.java


```

swagger editor  TODO
- "#/definitions/Bank"各类entity定义

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

- 订单超时？？？？？？？？？？？

  - 承运方trunk请求订单要送达接口，获取当前时间与订单被抢单的时间相减，差值与订单规定的运送时间对比。返回是否超时以及超时时长

  - Trunk承运方获取超时计费额度
  
  
  - 断线Notification：Push，Notification，Service Work
