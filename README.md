**基于SpringBoot实现Java高并发之反向拍卖物流抢单系统**

**技术栈**

* 后端(localhost:8080)： SpringBoot-2.x + Redis v4.09 + Mysql v14.14

* 前端(用于测试)： Bootstrap + Jquery


**测试环境**

* IDEA + Maven v3.6.0 + Tomcat8 + JDK8 + git v2.17.1
* 配置应该 t2.medium够

**启动说明**
* 如果不想安装mysql和rabbitmq组件，请在启动前安装其容器，命令行如下（前提安装docker和docker-compose）
`docker-compose -f docker/mysql.yml up -d`
`docker-compose -f docker/rabbitmq.yml up -d`
* adminer(localhost:8000)是web端的mysql管理工具，用户名/密码是root/1234，
* 如果已经安装相关组件，请配置好 [application.yml](https://github.com/TyCoding/springboot-seckill/blob/master/src/main/resources/application.yml) 中连接数据库的用户名和密码，以及Redis服务器的地址和端口信息。同时，请创建数据库`seckill`，建表SQL语句放在：db。

**[OpenAPI](https://documenter.getpostman.com/view/7656152/SVSNK85K?version=latest)**  
**[需求变更记录](https://docs.google.com/document/d/1EUJGxyS2pTmbTStve1gEUCYWkn1KCf-pr_MBShYaBlI/edit?usp=sharing)**

### 生产环境
```$xslt
java -jar target/springboot-seckill-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

* docker使用
`docker kill dockerName`
`dockcer rm dockerName`
* docker容器重启之后MySQL里面的数据将全部清空
* mac上的mysql的hostname是192.168.99.100,所以在Windows上开发时需要改回localhost
* 统一使用我服务器上的MySQL服务，不用改



- [x] 按年月日展示平台的收益-定时任务，线程池（大小为10）中选3个线程用于更新年月日收益

- [x] 只有当发货方资金充足才可以进行发布订单

- [x] 注册前检查注册人的id是否存在，如果存在则拒绝注册

- [x] 银行和担保方

- [x] 承运方、发货方评级

- [x] 资金结算全过程

- [x] 三种撤单方式

- [x] 平台参数的初始值设置

- [x] 平台状态的展示（包含当前注册车辆、发货方数量、正在执行的订单数量、资金等）;平台参数查看

- [x] 平台参数的设置

- [x] 获取某辆车当前的当前已接未运货物数量和正在运送货物数量

- [x]  按照条件查询订单：承运方id、发货方id、收货方id、货物状态（目前的需求包括异常订单、超时订单、正常完成的订单、正在发布的订单）

- [x]  3 已经在运  --df.format(new Date()) + "发送请求说明开始运该单

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
- [x] 每次验货一单需要更新货车的位置信息（将缓存内的信息同步到数据库中）truck同步数据
- [x] 更新货物信息的时候是更新缓存内的货物信息，需要在收货方验货的时候同步数据 cargo同步数据
- [x] bid在停止抢单请求的时候会将bid缓存与数据库同步，同时删除该cargoId对应的bid缓存


- [x] @ServerEndpoint("/websocket2/{userno}") 承运方websocket消息码 1--通知承运方抢到该单;2--通知承运方未抢到该单;3--通知承运方有新单可抢;4--转单成功通知；5--通知承运方的撤单成功请求;6\*订单号\*红包数值--红包通知
- [x] @ServerEndpoint("/websocket3/{userno}") 发货方websocket消息码 1\*订单号--装货运输;2\*订单号--确认交货;3\*订单号\*红包数目--红包通知;4\*订单号--收货方验货订单超时
- [x] @ServerEndpoint("/websocket4/{userno}") 收货方websocket消息码 1\*订单号--装货运输;2\*订单号--确认交货;3\*订单号--收货方验货订单超时



**货物状态**

      0 订单创建未发布 --创建订单
     
      1 订单发布中 --平台确定开抢
     
      2 已接未运 --平台确定最终truck抢单成功
     
      3 已经在运
     
      4 已运达等待收货
     
      5 转手中（正挂在平台公告栏上）--转单成功
     
      6 发布时无人接单撤单 --撤单
     
      7 已接未运撤单 --撤单
     
      8 订单完结 --确定收单时
     
      9 订单超时 --确定收单时
     
      10 订单异常 --确定收单时
     
      11 被正常转单，本订单已经失效
     
      12 该订单为第三类撤单产生的返程订单
     
      13 收货方未按时验货
      
      14 提醒验货时刻
           
     
**车队圈子功能：**

1 发布订单前检查订单是否是群发给所有承运方
2 如果是货物提交属性filed为"全体承运方"则群发给所有承运方；否则只发给与订单filed属性相同的承运方
     
     
**关于财务模拟的说明**

1 由于只做模拟，不提供银行账户的注册注销接口；不提供设置账户初始资金的接口（使用默认值）

2 在进行增减操作前，先判断有没有该账户，没有则自动注册

3 实现的接口包含：查询所有账户；查看某账户资金流水和开始和结束的金额

4 实现的功能：增减某一账户的可用金额（对应账户的冻结解冻资金）；
  增减某账户金额；查询某账户当前可用金额
  
**关于JWT**

1 所有的注册与登录均在/auth/**下

2 /auth/login/**进入 会返回一个token,会因此产生一个Token用于后序访问

3 数据库保存各方角色：承运方是1,发货方是2,收货方是3,平台方是4,前端直接访问正确的URI，并带上用户名/密码即可登录

4 平台帐号只有admin/admin

5 任何一方只要有Token均可访问任意api 方便前端调用
