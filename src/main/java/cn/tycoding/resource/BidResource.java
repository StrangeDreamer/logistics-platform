package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Bid;
import cn.tycoding.domain.Platform;
import cn.tycoding.exception.BidException;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.BidRepository;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.BidService;
import cn.tycoding.service.CargoService;
import cn.tycoding.service.TruckService;
import cn.tycoding.websocket.WebSocketServer;
import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private PlatformRepository platformRepository;
    @Autowired
    //private TruckRepository truckRepository;
    private TruckService truckService;
    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BidService bidService;
    @Autowired
    private WebSocketTest webSocketTest;
    /**抢单
     * redis维护键值对<cargoId,bid>
     *    <cargoId,Cargo>
     *
     *   TODO id=0因为请求过来的Bid并没有设置其主键id     获取map键值对：3-Bid(id=0, cargoId=3, bidPrice=89.0, truckId=2)
     *   TODO 设置全局变量 cargoKey 和bidsKey
     * @param bid
     * @return
     */
    @PostMapping
    public Map<String, Object> bidCargo(@RequestBody Bid bid) {
        Map<String, Object> result = new HashMap<String, Object>();
        //获取系统时间
        //保存竞价请求
        bidRepository.save(bid);

        Date nowTime = new Date();
        Cargo cargo=cargoService.findCargoById(bid.getCargoId());
        Platform platform = platformRepository.findRecentPltf();
        double  lowestBidPriceRatio = platform.getLowestBidPriceRatio();

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
        // TODO：担保额判断,太多！建议创建一个service，将下面的代码放进service里面
        logger.info("向第三方查询货车当前可用担保额是否超过" + cargo.getInsurance() + "，额度充足方可出价" );


        // 对出价的合法性进行判断：包含 货车类型和货物类型相对应; 出价金额范围合理;货物体积大小符合要求
        if (!cargo.getType().equals(truckService.findTruckById(bid.getTruckId()).getType())){
            //运输类型需要符合要求
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
            throw new BidException( "货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
        } else if (cargo.getVolume() > truckService.findTruckById(bid.getTruckId()).getAvailableVolume()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！体积超载");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！体积超载");
        } else if (cargo.getWeight() > truckService.findTruckById(bid.getTruckId()).getAvailableWeight()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！重量超载");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！重量超载");
        } else if ((bid.getBidPrice() > cargo.getFreightFare())
                || (bid.getBidPrice() < cargo.getFreightFare() * lowestBidPriceRatio)){
            // 出价区间合理
            logger.info("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！价格不合理！");
            throw new BidException("货车"+bid.getTruckId()+"对订单" + cargo.getId() + "出价无效！价格不合理！ 请在以下范围内出价："
            + cargo.getFreightFare() * lowestBidPriceRatio  + "~" + cargo.getFreightFare());
        }


        try {
            Bid redisbid=bidService.checkRedis(bid.getCargoId());
            if (redisbid==null){
                //存入redis缓存中(1个)。 key:秒杀表的ID值； value:秒杀表数据
                redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                //WebSocketServer.sendInfo("有人出价"+bid.getBidPrice());
            }
            else {
                //redisCargo更大,则需要更新
                if (redisbid.getBidPrice()>bid.getBidPrice()){
                    redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                    WebSocketServer.sendInfo("有人出价"+bid.getBidPrice());
                }
            }

            System.out.println(redisTemplate.boundHashOps(bidsKey).entries().size());
            redisTemplate.boundHashOps(bidsKey).entries().forEach((m,n)-> System.out.println("获取map键值对："+m+"-"+n));
            result.put("operationResult", "货车"+bid.getTruckId()+"对订单" + cargo.getId() + "的本次出价排队成功" +
                        "并扣除担保额度" + cargo.getInsurance());
            result.put("截止时间",cargo.getBidEndTime());
            logger.info("货车"+bid.getTruckId()+"由于对订单" + cargo.getId() + "的出价，" +
                    "扣除担保额度" + cargo.getInsurance());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }
        return result;
    }


    /**
     * 平台发送停止抢单请求
     * @param cargoId
     * @return 抢单结果
     */
    @GetMapping("/{cargoId}")
    public Cargo stopBid (@PathVariable int cargoId){

        Cargo cargo = cargoRepository.findCargoById(cargoId);

        //平台前端发过来的停止抢单命令的时间可能会在实际上的抢单截至时间
        Date nowTime = new Date();
        if(nowTime.getTime()>=cargo.getBidEndTime().getTime()){
            Platform platform = platformRepository.findRecentPltf();
            double exhibitionFee = platform.getExhibitionFee();
            Bid bidrd = bidService.checkRedis(cargoId);
            // 订单没有人抢/时间段内不存在有效出价：对于转单订单，转手订单原订单继续执行；对于最原始的订单，直接撤单
            if (bidrd == null){
                // 当precargo 为null，表示最原始的订单，直接撤单
                if (cargo.getPreCargoId() != null) {
                    logger.info("抢单时间段内无有效出价，自动撤单！展位费不予退回！");
                    // TODO: 冻结资金恢复
                    logger.info("由于无人接单自动撤单，发货方" + cargo.getShipperId() + "冻结的资金恢复" + cargo.getFreightFare());
                    cargo.setStatus(6);
                    cargoRepository.save(cargo);
                    return cargoRepository.findCargoById(cargoId);
                }
                // 当不为null，表示是转手后新创建当订单，需要恢复原来当订单，并将当前订单设为未抢订单直接撤单
                else {
                    Cargo preCargo = cargoRepository.findCargoById(cargo.getPreCargoId());
                    // 原订单重新设置为已接未运
                    preCargo.setStatus(2);
                    // 创建的转单设置为无人接单撤单
                    cargo.setStatus(6);
                    // TODO: 冻结资金恢复
                    logger.info("车辆" + cargo.getTruckId() + "的订单" + cargo.getPreCargoId() + "转手失败,展位费不予退回！");
                    logger.info("由于无人接单自动撤单，转手承运方" + cargo.getTruckId() + "冻结的资金恢复" + cargo.getFreightFare());
                    cargoRepository.save(cargo);
                    cargoRepository.save(preCargo);
                }
            }
            // 订单存在有效出价/有人抢单
            else {
                //将缓存中的最低价和抢单用户刷进cargo数据库中
                cargo.setBidPrice(bidrd.getBidPrice());
                cargo.setTruckId(bidrd.getTruckId());
                cargo.setStatus(2);

                // 判断是否是转单,如果是转手订单,需要额外进行资金结算，退换发货方展位费;原来的订单状态设置为正常完成
                if (cargo.getPreCargoId() != null) {
                    //通知转单成功
                    Cargo preCargo = cargoService.findCargoById(cargo.getPreCargoId());
                    preCargo.setStatus(11);
                    cargoRepository.save(preCargo);
                    logger.info("承运方{}转单成功",preCargo.getTruckId());
                    // TODO:展位费退换 担保额恢复
                    logger.info("订单被成功接单，平台返还转手承运方" + cargo.getTruckId() +"展位费" + exhibitionFee);
                    webSocketTest.sendToUser2(String.valueOf(preCargo.getTruckId()),"转单成功");
                    // 转手成功后，原来的车辆担保额恢复
                    logger.info("由于转手成功，转手承运方"+ cargo.getTruckId() + "恢复担保额度" + cargo.getInsurance());
                    // 转单成功的资金结算 归到最终结算
                }
                // 如果不是转手订单，只需要退换承运方展位费
                else {
                    // TODO:展位费退换
                    logger.info("订单被成功接单，平台返还发货方" + cargo.getShipperId() +"展位费" + exhibitionFee );
                }

                redisTemplate.boundHashOps(bidsKey).delete(cargoId);
                redisTemplate.boundHashOps(cargoKey).delete(cargoId);

                // 为没有中标的车辆 恢复担保额度:先找到本次出价的所有bid，对没有中标的bid的车辆恢复担保额
                List<Bid> bidlist = bidRepository.findAllByCargoId(cargoId);
                for (Bid bid: bidlist) {
                    if (bid.getId()!= bidrd.getId()){
                        // TODO：担保额恢复
                        logger.info("由于车辆" + bid.getTruckId() + "出价失败，担保额恢复" + cargoRepository.findCargoById(cargoId).getInsurance());
                        webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()),"抱歉，您没有抢到订单" + cargoId);
                    }
                    else
                    {
                        logger.info("该承运方{}抢到订单{}",bid.getTruckId(),cargoId);
                        //通知该在线用户抢单成功消息
                        webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()),"恭喜您抢到了订单" + cargoId);

                    }
                }
            }
        } else {
            logger.info("抢单时间还未截止！");
            throw new CargoException("抢单时间还未截止！");
        }
        cargoRepository.save(cargo);
        return cargo;
    }
}
