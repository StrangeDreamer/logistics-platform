package cn.tycoding.resource;

import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.*;
import cn.tycoding.exception.BidException;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.*;
import cn.tycoding.service.*;
import cn.tycoding.websocket.WebSocketTest;
import cn.tycoding.websocket.WebSocketTest3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Date;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/bids")

public class BidResource {

    private final Logger logger = LoggerFactory.getLogger(BidResource.class);

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
    private BidRepository bidRepository;
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private BidService bidService;
    @Autowired
    private WebSocketTest webSocketTest;
    @Autowired
    private WebSocketTest3 webSocketTest3;


    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private InspectionService inspectionService;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//         System.out.println(df.format(new Date()));// new Date()为获取当前系统时间


    /**
     * 抢单
     * redis维护键值对<cargoId,bid>
     * <cargoId,Cargo>
     * <p>
     * TODO id=0因为请求过来的Bid并没有设置其主键id     获取map键值对：3-Bid(id=0, cargoId=3, bidPrice=89.0, truckId=2)
     * TODO 设置全局变量 cargoKey 和bidsKey
     *
     * @param bid
     * @return
     */
    @MyLog(value = "抢单请求")
    @PostMapping
    public Map<String, Object> bidCargo(@RequestBody Bid bid) {
        Map<String, Object> result = new HashMap<String, Object>();

        // 获得承运方的担保账户
        InsuranceAccount insuranceAccount = insuranceAccountService.check(bid.getTruckId(), "truck");

        Date nowTime = new Date();
        Cargo cargo = cargoService.findCargoById(bid.getCargoId());
        Platform platform = platformRepository.findRecentPltf();
        double lowestBidPriceRatio = platform.getLowestBidPriceRatio();
        bid.setPriceCorrect(1);

        if (nowTime.getTime() > cargo.getBidEndTime().getTime()) {
            logger.info("错过抢单时间");
            throw new BidException("错过抢单截止时间：" + cargo.getBidEndTime());
        }
        if (nowTime.getTime() < cargo.getBidStartTime().getTime()) {
            logger.info("还未开抢");
            throw new BidException("还未开抢，开抢时间：" + cargo.getBidStartTime());
        }

        Truck truck = truckRepository.findTruckById(bid.getTruckId());
        // 对出价的合法性进行判断：包含 货车类型和货物类型相对应; 出价金额范围合理;货物体积大小符合要求；承运方需要激活;担保额度是否充足
        if (    !(cargo.getType().equals(truck.getType()) ||
                    (cargo.getType().equals("普通") && truck.getType().equals("冷链")))
        ) {
            //运输类型需要符合要求
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！运输类型不符合要求！");
        } else if (cargo.getVolume() > truck.getAvailableVolume()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！体积超载");
            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！体积超载");
        } else if (cargo.getWeight() > truck.getAvailableWeight()) {
            //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！重量超载");
            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！重量超载");
        } else if ((bid.getBidPrice() > cargo.getFreightFare())
                || (bid.getBidPrice() < cargo.getFreightFare() * lowestBidPriceRatio)) {
            // 出价区间不合理
            bid.setPriceCorrect(-1);
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！价格不合理！");
//            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！价格不合理！ 请在以下范围内出价："
//                    + cargo.getFreightFare() * lowestBidPriceRatio + "~" + cargo.getFreightFare());
//            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！价格不合理！");

        } else if (!truck.isActivated()) {
            // 车辆激活才可以接单
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！该承运方尚未激活！");
            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！该承运方尚未激活！");
        } else if (insuranceAccountService.getAvailableMoney(truck.getId(), "truck") < cargo.getInsurance()) {
            // 车辆剩余担保额充足才可以接单
            logger.info("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！该承运方的担保额度不足！\n " +
                    "当前该货车可用担保额度为" + insuranceAccountService.getAvailableMoney(truck.getId(), "truck") +
                    "货物要求担保额为" + cargo.getInsurance());
            throw new BidException("货车" + bid.getTruckId() + "对订单" + cargo.getId() + "出价无效！该承运方的担保额度不足！\n " +
                    "当前该货车可用担保额度为" + insuranceAccountService.getAvailableMoney(truck.getId(), "truck") +
                    "货物要求担保额为" + cargo.getInsurance());
        }

        // 对运输路径是否会出现超时预警进行判断,已经在前端实现

        //获取系统时间
        //保存竞价请求
        bidRepository.save(bid);
        try {
            Bid redisbid = bidService.checkRedis(bid.getCargoId());
            if (redisbid == null) {
                //存入redis缓存中(1个)。 key:秒杀表的ID值； value:秒杀表数据
                redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                //WebSocketServer.sendInfo("有人出价"+bid.getBidPrice());
            } else {
                //redisCargo更大,则需要更新
                if (redisbid.getBidPrice() > bid.getBidPrice()) {
                    redisTemplate.boundHashOps(bidsKey).put(bid.getCargoId(), bid);
                    //WebSocketServer.sendInfo("有人出价"+bid.getBidPrice());
                }
            }

            System.out.println(redisTemplate.boundHashOps(bidsKey).entries().size());
            redisTemplate.boundHashOps(bidsKey).entries().forEach((m, n) -> System.out.println("获取map键值对：" + m + "-" + n));
            result.put("operationResult", "货车" + bid.getTruckId() + "对订单" + cargo.getId() + "的本次出价排队成功" +
                    "并扣除担保额度" + cargo.getInsurance());
            result.put("截止时间", cargo.getBidEndTime());
            logger.info("货车" + bid.getTruckId() + "由于对订单" + cargo.getId() + "的出价，" +
                    "扣除担保额度" + cargo.getInsurance());

            insuranceAccount.setInsuranceAccountLog(insuranceAccount.getInsuranceAccountLog() +
                    "\n" + df.format(new Date()) + "货车" + bid.getTruckId() + "由于对订单" + cargo.getId() + "的出价");
            insuranceAccountService.changeAvailableMoney(truck.getId(), "truck", (0 - cargo.getInsurance()));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("operationResult", "排队失败");
        }
        return result;
    }

    /**
     * 平台发送停止抢单请求
     *
     * @param cargoId
     * @return 抢单结果
     */
    @MyLog(value = "平台发送停止抢单请求")
    @GetMapping("/{cargoId}")
    public Cargo stopBid(@PathVariable int cargoId) {

        Cargo cargo = cargoRepository.findCargoById(cargoId);

        // 获取发货方的银行账户
        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");

        // 获取平台银行账户
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");

        //平台前端发过来的停止抢单命令的时间可能会在实际上的抢单截至时间
        Date nowTime = new Date();
//        if (nowTime.getTime() >= cargo.getBidEndTime().getTime()) {
        int t=(platformRepository.findRecentPltf().getBidingDuration()/3600);
        Date bidEndTimeSpeedup = new Date(cargo.getBidStartTime().getTime()+t);

        if (nowTime.getTime() >= bidEndTimeSpeedup.getTime()) {
            Platform platform = platformRepository.findRecentPltf();
            double exhibitionFee = platform.getExhibitionFee();
            Bid bidrd = bidService.checkRedis(cargoId);
            if (cargo.getStatus() == 6) {
                return cargo;
            }
            // 订单没有人抢/时间段内不存在有效出价：对于转单订单，转手订单原订单继续执行；对于最原始的订单，直接撤单
            if (bidrd == null) {
                // 当precargo 为null，表示最原始的订单，直接撤单

                // TODO:无人出价，通知发货方无人接单
                webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()),"7*" + cargo.getId());

                if (cargo.getPreCargoId() == null) {
                    logger.info("抢单时间段内无有效出价，自动撤单！展位费不予退回！");
                    // 冻结资金恢复
                    bankAccountService.addMoneyLog(bankAccountShipper,
                            df.format(new Date()) + "  由于订单" + cargo.getId()
                                    + "无人接单自动撤单，发货方" + cargo.getShipperId() + "冻结的资金恢复");
                    bankAccountService.changeAvailableMoney(bankAccountShipper, cargo.getFreightFare());
                    logger.info("由于无人接单自动撤单，发货方" + cargo.getShipperId() + "冻结的资金恢复");

                    bankAccountService.addMoneyLog(bankAccountShipper,
                            df.format(new Date()) + "  由于订单" + cargo.getId()
                                    + "无人接单自动撤单，发货方" + cargo.getShipperId() + "之前支付的展位费不予退换");

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
                    // 冻结资金恢复
                    BankAccount bankAccountTruck = bankAccountService.check(preCargo.getTruckId(), "truck");
                    bankAccountService.addMoneyLog(bankAccountTruck,
                            df.format(new Date()) + "  由于订单" + cargo.getId()
                                    + "无人接单自动撤单，发货承运方" + preCargo.getTruckId() + "冻结的资金恢复");
                    bankAccountService.changeAvailableMoney(bankAccountTruck, cargo.getFreightFare());

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

                // 判断是否是转单,如果是转手订单,需要:退还发货承运方展位费;原来的订单进行结算；
                if (cargo.getPreCargoId() != null) {
                    //通知转单成功
                    Cargo preCargo = cargoService.findCargoById(cargo.getPreCargoId());
                    preCargo.setStatus(11);
                    cargoRepository.save(preCargo);
                    logger.info("承运方{}转单成功", preCargo.getTruckId());

                    BankAccount bankAccountTruck = bankAccountService.check(preCargo.getTruckId(), "truck");
                    InsuranceAccount insuranceAccount = insuranceAccountService.check(preCargo.getTruckId(), "truck");

                    // 不管是否为转单，在接单后返还展位费的时候都需要利润空间大于手续费才可以免除手续费
                    double moneyReturn = exhibitionFee;
                    if (platform.getHandlingFee() > (cargo.getFreightFare() - cargo.getBidPrice())) {
                        moneyReturn = exhibitionFee - platform.getHandlingFee();
                    }

                    // 展位费退还给承运方
                    bankAccountService.addMoneyLog(bankAccountPlatform,
                            df.format(new Date()) + "  由于订单" + cargo.getId() + "被成功接单，平台返还转手承运方" + cargo.getTruckId() + "展位费");
                    bankAccountService.addMoneyLog(bankAccountTruck,
                            df.format(new Date()) + "  由于订单" + cargo.getId() + "被成功接单，平台返还转手承运方" + cargo.getTruckId() + "展位费");
                    bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, moneyReturn);

                    // 由于一单一结 担保额不在此处重复恢复
                    // 承运方上一单的担保额恢复
//                    insuranceAccountService.addMoneyLog(insuranceAccount,
//                            df.format(new Date()) + "  由于订单" + cargo.getId() + "转手成功，转手承运方" + cargo.getTruckId() + "恢复担保额度");
//                    insuranceAccountService.changeAvailableMoney(insuranceAccount, preCargo.getInsurance());

                    logger.info("订单被成功接单，平台返还转手承运方" + cargo.getTruckId() + "展位费" + exhibitionFee);
                    //webSocketTest.sendToUser2(String.valueOf(preCargo.getTruckId()),"转单成功");
                    webSocketTest.sendToUser3(String.valueOf(preCargo.getTruckId()), 4);
                    // 转手成功后，原来的车辆担保额恢复
                    logger.info("由于转手成功，转手承运方" + cargo.getTruckId() + "恢复担保额度" + cargo.getInsurance());
                    // 转单成功的资金结算 归到最终结算

                    // 一单一结算 转单成功后，上一个订单自动完成
                    Inspection inspection = new Inspection();
                    inspection.setCargoId(preCargo.getId());
                    inspection.setInspectionResult(8);
                    inspection.setTimeoutPeriod(0);
                    preCargo.setStatus(4);
                    cargoRepository.save(preCargo);
                    inspectionService.inspectionCargo(inspection);

                }
                // 如果不是转手订单，只需要退换发货方展位费
                else {

                    // 不管是否为转单，在接单后返还展位费的时候都需要利润空间大于手续费才可以免除手续费
                    double moneyReturn = exhibitionFee;
                    if (platform.getHandlingFee() > (cargo.getFreightFare() - cargo.getBidPrice())) {
                        moneyReturn = exhibitionFee - platform.getHandlingFee();
                    }

                    // 展位费退还给发货
                    bankAccountService.addMoneyLog(bankAccountPlatform,
                            df.format(new Date()) + "  订单" + cargo.getId() + "被成功接单，平台返还发货方" + cargo.getTruckId() + "展位费");
                    bankAccountService.addMoneyLog(bankAccountShipper,
                            df.format(new Date()) + "  订单" + cargo.getId() + "被成功接单，平台返还发货方" + cargo.getTruckId() + "展位费");
                    bankAccountService.transferMoney(bankAccountPlatform, bankAccountShipper, moneyReturn);
                    logger.info("订单被成功接单，平台返还发货方" + cargo.getShipperId() + "展位费" + moneyReturn);
                }

                redisTemplate.boundHashOps(bidsKey).delete(cargoId);
//                redisTemplate.boundHashOps(cargoKey).delete(cargoId);
                cargoService.delCargoRedis(cargoId);
                // 为没有中标的车辆 恢复担保额度:先找到本次出价的所有bid，对没有中标的bid的车辆恢复担保额
                List<Bid> bidlist = bidRepository.findAllByCargoId(cargoId);

                int n = bidlist.size();
                // 存在有效出价

                if (bidrd.getBidPrice() <= cargo.getFreightFare()) {
                    // TODO:存在有效出价，通知发货方n个人出价，订单已经被接单
                    webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()),"8*" + cargo.getId() + "*" + n);
                    for (Bid bid : bidlist) {
                        if (bid.getId() != bidrd.getId()) {
                            // 担保额恢复
                            InsuranceAccount insuranceAccount = insuranceAccountService.check(bid.getTruckId(), "truck");
                            insuranceAccountService.addMoneyLog(insuranceAccount,
                                    df.format(new Date())
                                            + "  由于车辆" + bid.getTruckId() + "对订单" + cargo.getId() + "出价失败，车辆担保额恢复");
                            insuranceAccountService.changeAvailableMoney(insuranceAccount, cargo.getInsurance());
                            logger.info("由于车辆" + bid.getTruckId() + "出价失败，担保额恢复" + cargoRepository.findCargoById(cargoId).getInsurance());
//                        webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()),"抱歉，您没有抢到订单" + cargoId);
                            if (bid.getPriceCorrect() == 1) {
                                //TODO: 通知承运方n个人出价，您没有抢到订单，因为有人出价比您低
                                webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()), "7*" + n + "*" + bid.getBidPrice());
                            } else {
                                // TODO：通知承运方n个人出价，您没有抢到订单，因为您的出价不合理
                                webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()), "8*" + n + "*" + bid.getBidPrice());
                            }
                        } else {
                            // TODO：通知承运方n个人出价，抢到了订单
                            webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()), "9*" + n + "*" + bid.getBidPrice());
                        }
                    }
                }
                // 不存在有效出价
                else {
                    // TODO: 不存在有效出价，通知发货方n个人出价，但无有效出价。
                    webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()),"9*" + cargo.getId() + "*" + n );
                    for (Bid bid : bidlist) {
                        // TODO:通知承运方n个人出价，您的出价不合理。
                        webSocketTest.sendToUser2(String.valueOf(bid.getTruckId()), "8*" + n + "*" + bid.getBidPrice());
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
