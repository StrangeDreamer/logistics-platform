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
.
├── README  -- Doc文档
├── db  -- 数据库约束文件
├── mvnw  
├── mvnw.cmd
├── pom.xml  -- 项目依赖
└── src
    ├── main
    │   ├── java
    │   │   └── cn
    │   │       └── tycoding
    │   │           ├── SpringbootSeckillApplication.java  -- SpringBoot启动器
    │   │           ├── controller  -- MVC的web层
    │   │           ├── dto  -- 统一封装的一些结果属性，和entity类似
    │   │           ├── entity  -- 实体类
    │   │           ├── enums  -- 手动定义的字典枚举参数
    │   │           ├── exception  -- 统一的异常结果
    │   │           ├── mapper  -- Mybatis-Mapper层映射接口，或称为DAO层
    │   │           ├── redis  -- redis,jedis 相关配置
    │   │           └── service  -- 业务层
    │   └── resources
    │       ├── application.yml  -- SpringBoot核心配置
    │       ├── mapper  -- Mybatis-Mapper层XML映射文件
    │       ├── static  -- 存放页面静态资源，可通过浏览器直接访问
    │       │   ├── css
    │       │   ├── js
    │       │   └── lib
    │       └── templates  -- 存放Thymeleaf模板引擎所需的HTML，不能在浏览器直接访问
    │           ├── page
    │           └── public  -- HTML页面公共组件（头部、尾部）
    └── test  -- 测试文件
```

swagger editor  TODO
- "#/definitions/Bank"各类entity定义

- 抢单包含如下API
   - Sender创建POST了一个订单，同时将所有订单放进Redis中（或者队列中）
     - POST /sender
   - 承运方Trunk查询所有可以抢的订单
     - GET /truck/cargos
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