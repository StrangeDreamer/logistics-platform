package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Bid;
import cn.tycoding.exception.BidException;
import cn.tycoding.repository.BidRepository;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.BidService;
import cn.tycoding.service.CargoService;
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
@RequestMapping("/bids")
public class BidResource {

    private final Logger logger=LoggerFactory.getLogger(BidResource.class);

    //设置秒杀redis缓存的key
    private final String bidsKey = "bids";
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private BidService bidService;
    /**抢单
     * redis维护键值对<cargoId,bid>
     *    <cargoId,Cargo>
     *
     *   TODO id=0因为请求过来的Bid并没有设置其主键id     获取map键值对：3-Bid(id=0, cargoId=3, orderPrice=89.0, truckId=2)
     *   TODO 设置全局变量 cargoKey 和bidsKey
     * @param bid
     * @return
     */
    @PostMapping
    public Map<String, Object> bidCargo(@RequestBody Bid bid) {
        Map<String, Object> result = new HashMap<String, Object>();
        //获取系统时间

        Date nowTime = new Date();
        Cargo cargo=cargoService.findCargoById(bid.getCargoId());

        if (nowTime.getTime()>cargo.getBidEndTime().getTime()){
            logger.info("错过抢单时间");
            throw new BidException("错过抢单截止时间："+cargo.getBidEndTime());
        }
        if (nowTime.getTime()<cargo.getBidStartTime().getTime()){
            logger.info("还未开抢");
            throw new BidException("还未开抢，开抢时间："+cargo.getBidStartTime());
        }

   /*     try {

            long hashIncLong=redisTemplate.opsForHash().increment("hashInc",bidsKey,1);

            redisTemplate.boundHashOps(bidsKey).put(hashIncLong, bid);
            System.out.println(redisTemplate.boundHashOps(bidsKey).entries().size());
            redisTemplate.boundHashOps(bidsKey).entries().forEach((m,n)-> System.out.println("获取map键值对："+m+"-"+n));

            result.put("operationResult", "排队成功");
            result.put("截止时间",cargo.getBidEndTime());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }*/
        // TODO：担保额判断
        logger.info("向第三方查询货车当前可用担保额是否超过" + cargo.getInsurance() + "，额度充足方可出价" );


        // 对出价的合法性进行判断：包含 货车类型和货物类型相对应; 出价金额范围合理;货物体积大小符合要求
        if (!cargo.getType().equals(truckRepository.findTruckById(bid.getTruckId()).getType())){
            //运输类型需要符合要求
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
            throw new BidException( "货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
        } else if (cargo.getVolume() > truckRepository.findTruckById(bid.getTruckId()).getAvailableVolume()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！体积超载");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！体积超载");
        } else if (cargo.getWeight() > truckRepository.findTruckById(bid.getTruckId()).getAvailableWeight()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！重量超载");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！重量超载");
        } else if ((bid.getOrderPrice() > cargo.getFreightFare())
                || (bid.getOrderPrice() < cargo.getFreightFare() * 0.40)){
            // 出价区间合理  TODO: 出价下限默认为40%，如何设置这个参数
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！价格不合理！");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！价格不合理！");
        }


        try {
            Bid redisbid = (Bid) redisTemplate.boundHashOps(bidsKey).get(bid.getCargoId());

            if (redisbid==null){
                //存入redis缓存中(1个)。 key:秒杀表的ID值； value:秒杀表数据
                redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                WebSocketServer.sendInfo("有人出价"+bid.getOrderPrice());
            }
            else {
                //redisCargo更大,则需要更新
                if (redisbid.getOrderPrice()>bid.getOrderPrice()){
                    redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                    WebSocketServer.sendInfo("有人出价"+bid.getOrderPrice());
                }
            }
            //保存竞价请求
            bidService.saveBid(bid);
            System.out.println(redisTemplate.boundHashOps(bidsKey).entries().size());
            redisTemplate.boundHashOps(bidsKey).entries().forEach((m,n)-> System.out.println("获取map键值对："+m+"-"+n));
            result.put("operationResult", "货车"+bid.getTruckId()+"对订单" + cargo.getId() + "的本次出价排队成功" +
                        "并扣除担保额度" + cargo.getInsurance());
            result.put("截止时间",cargo.getBidEndTime());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }
        return result;
    }


    @GetMapping("/stopBid/{cargoId}")
    public Cargo stopBid (@PathVariable int cargoId){
        Bid bidrd= (Bid) redisTemplate.boundHashOps(bidsKey).get(cargoId);
        Cargo cargo=cargoService.findCargoById(cargoId);
        //平台前端发过来的停止抢单命令的时间可能会在实际上的抢单截至时间

        Date nowTime = new Date();
        if(nowTime.getTime()>=cargo.getBidEndTime().getTime()){
            //将缓存中的最低价和抢单用户刷进cargo数据库中
            cargo.setOrderPrice(bidrd.getOrderPrice());
            cargo.setTruckId(bidrd.getTruckId());
            cargo.setStatus(2);
            cargoRepository.save(cargo);
            redisTemplate.boundHashOps(bidsKey).delete(cargoId);
            redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        }
        return cargo;
    }
}
