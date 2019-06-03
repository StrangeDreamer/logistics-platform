package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.CargoOrderLite;
import cn.tycoding.exception.CargoException;
import cn.tycoding.exception.CargoOrderException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cargoOrders")
public class CargoOrderResource {

    private final Logger logger=LoggerFactory.getLogger(CargoOrderResource.class);

    //设置秒杀redis缓存的key
    private final String cargoOrdersKey = "CargoOrders";

    private final String cargoKey = "Cargo";

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CargoRepository cargoRepository;

    /**抢单
     * redis维护键值对<cargoId,cargoOrderLite>
     *    <cargoId,Cargo>
     *
     *   TODO id=0?????     获取map键值对：3-CargoOrderLite(id=0, cargoId=3, orderPrice=89.0, truckId=2)
     *   TODO 设置全局变量 cargoKey 和cargoOrdersKey
     * @param cargoOrderLite
     * @return
     */
    @PostMapping
    public Map<String, Object> chaseCargo(@RequestBody CargoOrderLite cargoOrderLite) {
        Map<String, Object> result = new HashMap<String, Object>();
        //获取系统时间
        Date nowTime = new Date();
        //将要抢的订单放进redis中
        Cargo redisCargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(cargoOrderLite.getCargoId());
        if (redisCargo==null){

            Cargo cargo=cargoRepository.findById(cargoOrderLite.getCargoId()).orElseThrow(()->new CargoException("this cargo is not exist!"));
            logger.info("开抢时间"+cargo.getStartTime().toString());
            redisTemplate.boundHashOps(cargoKey).put(cargoOrderLite.getCargoId(),cargo);
        }
        redisCargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(cargoOrderLite.getCargoId());


        if (nowTime.getTime()>redisCargo.getEndTime().getTime()){
            logger.info("错过抢单时间");
            throw new CargoOrderException("错过抢单时间");
        }
        if (nowTime.getTime()<redisCargo.getStartTime().getTime()){
            logger.info("还未开抢");
            throw new CargoOrderException("还未开抢");
        }
        try {
            CargoOrderLite redisCargoOrder = (CargoOrderLite) redisTemplate.boundHashOps(cargoOrdersKey).get(cargoOrderLite.getCargoId());
            if (redisCargoOrder==null){
                //存入redis缓存中(1个)。 key:秒杀表的ID值； value:秒杀表数据
                redisTemplate.boundHashOps(cargoOrdersKey).put(cargoOrderLite.getCargoId(), cargoOrderLite);
                WebSocketServer.sendInfo("有人出价"+cargoOrderLite.getOrderPrice());
            }
            else {
                //redisCargo更大,则需要更新
                if (redisCargoOrder.getOrderPrice()>cargoOrderLite.getOrderPrice()){
                    redisTemplate.boundHashOps(cargoOrdersKey).put(cargoOrderLite.getCargoId(), cargoOrderLite);
                    WebSocketServer.sendInfo("有人出价"+cargoOrderLite.getOrderPrice());
                }
            }

            System.out.println(redisTemplate.boundHashOps(cargoOrdersKey).entries().size());
            redisTemplate.boundHashOps(cargoOrdersKey).entries().forEach((m,n)-> System.out.println("获取map键值对："+m+"-"+n));

            result.put("operationResult", "排队成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }
        return result;
    }
}
