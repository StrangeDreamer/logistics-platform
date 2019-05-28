package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.CargoOrderLite;
import cn.tycoding.exception.CargoOrderException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cargoOrders")
public class CargoOrderResource {

    private final Logger logger=LoggerFactory.getLogger(CargoOrderResource.class);

    //设置秒杀redis缓存的key
    private final String key = "CargoOrders";

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CargoRepository cargoRepository;

    @PostMapping
    public Map<String, Object> chaseCargo(@RequestBody CargoOrderLite cargoOrderLite) {
        Map<String, Object> result = new HashMap<String, Object>();
        //获取系统时间
        Date nowTime = new Date();
        Cargo cargo=cargoRepository.findById(cargoOrderLite.getCargoId()).get();

        if (nowTime.getTime()>cargo.getStartTime().getTime()){
            logger.info("错过抢单时间");
            throw new CargoOrderException("错过抢单时间");
        }
        try {
            logger.info("请求成功加入Redis缓存");
            CargoOrderLite redisCargo = (CargoOrderLite) redisTemplate.boundHashOps(key).get(cargoOrderLite.getCargoId());
            if (redisCargo==null){
                //存入redis缓存中(1个)。 key:秒杀表的ID值； value:秒杀表数据
                redisTemplate.boundHashOps(key).put(cargoOrderLite.getCargoId(), cargoOrderLite);
                WebSocketServer.sendInfo("有人出价"+cargoOrderLite.getCostPrice());
            }
            else {
                //redisCargo更大,则需要更新
                if (redisCargo.getCostPrice().compareTo(cargoOrderLite.getCostPrice())==1){
                    redisTemplate.boundHashOps(key).put(cargoOrderLite.getCargoId(), cargoOrderLite);
                    WebSocketServer.sendInfo("有人出价"+cargoOrderLite.getCostPrice());
                }
            }

            System.out.println(redisTemplate.boundHashOps(key).entries().size());
            redisTemplate.boundHashOps(key).entries().forEach((m,n)-> System.out.println("获取map键值对："+m+"-"+n));

            result.put("operationResult", "排队成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }
        return result;
    }
}
