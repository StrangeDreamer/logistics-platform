package cn.tycoding.resource;

import cn.tycoding.domain.CargoOrder;
import cn.tycoding.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cargoOrders")
public class CargoOrderResource {

    private final Logger logger=LoggerFactory.getLogger(CargoOrderResource.class);

    //设置秒杀redis缓存的key
    private final String key = "cargoOrders";

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Map<String, Object> chaseCargo(@RequestBody CargoOrder cargoOrder) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            WebSocketServer.sendInfo("有人出价"+cargoOrder.getCostPrice());
            //存入redis缓存中。 key:秒杀表的ID值； value:秒杀表数据
            redisTemplate.boundHashOps(key).put(cargoOrder.getId(), cargoOrder);
            logger.info("请求成功加入Redis缓存");

            result.put("operationResult", "排队成功");
        } catch (Exception e) {
            result.put("operationResult", "排队失败");
        }
        return result;
    }
}
