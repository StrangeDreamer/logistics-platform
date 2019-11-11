
package cn.tycoding.service;

import cn.tycoding.domain.*;
import cn.tycoding.exception.InspectionException;
import cn.tycoding.repository.*;
import cn.tycoding.websocket.WebSocketTest;
import cn.tycoding.websocket.WebSocketTest3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

import cn.tycoding.service.BonusService;

/**
 * @auther qlXie
 * @date 2019-06-12 11:00
 */

@Service
@Transactional
public class InspectionService {
    private final Logger logger = LoggerFactory.getLogger(InspectionService.class);
    @Autowired
    private InspectionRepository inspectionRepository;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private ShipperRepository shipperRepository;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private BonusService bonusService;
    @Autowired
    private WebSocketTest webSocketTest;
    @Autowired
    private WebSocketTest3 webSocketTest3;


    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式


    // 验货
    @Transactional
    public String inspectionCargo(Inspection inspection) {


        logger.info(platformRepository.findById(5).toString());

        Platform platform = platformRepository.findRecentPltf();

        double overTimeFeeRatio = platform.getOverTimeFeeRatio();
        double platformProfitRatio = platform.getPlatformProfitRatio();
        double shipperProfitRatio = platform.getShipperProfitRatio();
        double truckProfitRatio = platform.getTruckProfitRatio();
        String result = "验货结果：";
        Cargo cargo = cargoRepository.findCargoById(inspection.getCargoId());

        if (cargo.getStatus() != 4 && cargo.getStatus() != 14) {
            logger.info("货物当前状态不允许验货！");
            throw new InspectionException("货物当前状态不允许验货！");
        }

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");

        cargo.setStatus(inspection.getInspectionResult());
        cargoRepository.save(cargo);

//     * 8 正常完成
//     * 9 订单超时
//     * 10 订单异常
        // 验货异常/不通过直接抛出
        if (inspection.getInspectionResult() == 10) {

            // 异常的时候 暂时不对评级进行变化
            result = "货物出现异常，交给第三方处理";

            cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date()) + " 收货方成功对订单"
                    + cargo.getId() + "进行验收，验收结果为：货物出现异常，交给第三方处理");
            cargoRepository.save(cargo);
            return result;

        } else if (inspection.getInspectionResult() != 8 && inspection.getInspectionResult() != 9) {
            logger.info("验货结果设置错误！");
            throw new InspectionException("验货结果设置错误！");
        }

        Truck truck = truckRepository.findTruckById(cargo.getTruckId());

        // 如果超时，则先结算超时赔偿
        if (inspection.getInspectionResult() == 9) {
            // 订单超时，令承运方评级降低

            if (truck.getRanking() >= 1) {
                truck.setRanking(truck.getRanking() - 1);
            }

            double compensation = cargo.getInsurance() * (overTimeFeeRatio + inspection.getTimeoutPeriod() * 0.01);
            result = "验货正常但出现超时！\n" +
                    "超时时长为：" + inspection.getTimeoutPeriod() + "\n承运方" + cargo.getTruckId() + "需要支付赔偿金：" +
                    String.format("%.2f", compensation) + ";\n承运方" + cargo.getTruckId() + "的担保额度恢复仅当超时赔偿金支付完成后恢复！\n";
            BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
            InsuranceAccount insuranceAccount = insuranceAccountService.check(cargo.getTruckId(), "truck");

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    df.format(new Date()) + "  由于承运方" + truck.getId() +
                            "对订单" + cargo.getId() + "运输超时" + inspection.getTimeoutPeriod() + "单位时间");
            bankAccountService.addMoneyLog(bankAccountShipper,

                    df.format(new Date()) + "  由于承运方" + truck.getId() +
                            "对订单" + cargo.getId() + "运输超时" + inspection.getTimeoutPeriod() + "单位时间");
            bankAccountService.addMoneyLog(bankAccountTruck,
                    df.format(new Date()) + "  由于承运方" + truck.getId() +
                            "对订单" + cargo.getId() + "运输超时" + inspection.getTimeoutPeriod() + "单位时间");

            bankAccountService.transferMoney(bankAccountTruck, bankAccountPlatform, compensation);
            bankAccountService.transferMoney(bankAccountPlatform, bankAccountShipper, compensation);

            cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date())
                    + " 收货方" + cargo.getReceiverId()
                    + "成功对订单" + cargo.getId()
                    + "进行验收，验收结果为：订单超时" + inspection.getTimeoutPeriod() + "小时");
            cargoRepository.save(cargo);
        }
        // 没有超时则恢复担保额
        else {
            // 正常运达则评级增加
            if (truck.getRanking() <= 0.5)
                truck.setRanking(truck.getRanking() + 0.5);
            result = "验货正常没有出现超时！\n" + "担保额恢复" + cargo.getInsurance() + "\n";

            InsuranceAccount insuranceAccountLastTruck = insuranceAccountService.check(cargo.getTruckId(), "truck");
            // 正常运达恢复担保额
            insuranceAccountService.addMoneyLog(insuranceAccountLastTruck,
                    df.format(new Date())
                            + "  由于承运方" + truck.getId()
                            + "对订单" + cargo.getId()
                            + "正常运达，承运方" + truck.getId() + "恢复担保额");
            insuranceAccountService.changeAvailableMoney(insuranceAccountLastTruck, cargo.getInsurance());
            cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date())
                    + "收货方成功对订单" + cargo.getId()
                    + "进行验收，验收结果为：订单正常完成 ");
            cargoRepository.save(cargo);
        }

        /**
         * 转单部分的资金结算
         */
        // 不停结算，直到追溯到发货方；此处的均为承运方与承运方之间的资金流动
        if (cargo.getPreCargoId() != null) {
            result = result + "\n进入转单结算\n";
            double preFare = cargo.getPreFare();
            double freightFare = cargo.getFreightFare();
            double bidPrice = cargo.getBidPrice();
            Cargo preCargo = cargoRepository.findCargoById(cargo.getPreCargoId());

            // 转单部分的酬劳结算
            // 创建账户
            BankAccount bankAccountPreTruck = bankAccountService.check(preCargo.getTruckId(), "truck");
            BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");

            // 1 首先前一个承运方支付运费preFare给平台
            logger.info("承运方" + preCargo.getTruckId() + "向平台支付运费" + preFare);
            // 2 然后平台向后一个承运方支付运费bidprice
            logger.info("平台向承运方" + cargo.getTruckId() + "支付酬劳" + bidPrice);
            // 3 计算利润空间
            double profitSpace = freightFare - bidPrice;
            // 4 计算各方分配的收益
            double paltformProfit = platformProfitRatio * profitSpace;
            double truck1Profit = shipperProfitRatio * profitSpace;
            double truck2Profit = truckProfitRatio * profitSpace;

            // 5 计算各方评级 以及真正的利润
            int rank1 = (int) truckRepository.findTruckById(preCargo.getTruckId()).getRanking();
            int rank2 = (int) truckRepository.findTruckById(cargo.getTruckId()).getRanking();

            double trueTruck1ProfitTemp = truck1Profit * rank1 * 0.1;
            double trueTruck2ProfitTemp = truck2Profit * rank2 * 0.1;

            double bonusMaxRatioInFare = platform.getBonusMaxRatioInFare();
            double maxbonux = bonusMaxRatioInFare * cargo.getFreightFare();
            // 6 获取利润分配资金与利润分配上限的最低值作为真正的利润分配
            double trueTruck1Profit = trueTruck1ProfitTemp < maxbonux ? trueTruck1ProfitTemp : maxbonux;
            double trueTruck2Profit = trueTruck2ProfitTemp < maxbonux ? trueTruck2ProfitTemp : maxbonux;

            logger.info("平台向原转单承运方" + preCargo.getTruckId() + "支付利润分配" + truck1Profit);
            logger.info("平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit);

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");
            bankAccountService.addMoneyLog(bankAccountPreTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");
            bankAccountService.addMoneyLog(bankAccountTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");

            // 完成订单后先解冻资金
            bankAccountService.changeAvailableMoney(bankAccountPreTruck,freightFare);
            bankAccountService.addMoneyLog(bankAccountPreTruck,"\n");
            bankAccountService.transferMoney(bankAccountPreTruck, bankAccountPlatform, freightFare);
            bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, bidPrice);

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配");
            bankAccountService.addMoneyLog(bankAccountPreTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配");
            bankAccountService.addMoneyLog(bankAccountTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配");

            webSocketTest.sendToUser2(String.valueOf(bankAccountPreTruck.getId()), "6*" + String.valueOf(cargo.getId()) + "*"  + String.valueOf(trueTruck1Profit));
            webSocketTest.sendToUser2(String.valueOf(bankAccountTruck.getId()), "6*" + String.valueOf(cargo.getId()) + "*"  + String.valueOf(trueTruck2Profit));

            bankAccountService.transferMoney(bankAccountPlatform, bankAccountPreTruck, trueTruck1Profit);
            bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, trueTruck2Profit);

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    "------------------------------------\n" +
                            " 现有资金为" + bankAccountPlatform.getMoney()
                            + "    可用余额为" + bankAccountPlatform.getAvailableMoney());
            bankAccountService.addMoneyLog(bankAccountTruck,
                    "------------------------------------\n"
                            + " 现有资金为" + bankAccountTruck.getMoney()
                            + "    可用余额为" + bankAccountTruck.getAvailableMoney());
            bankAccountService.addMoneyLog(bankAccountPreTruck,
                    "------------------------------------\n"
                            + " 现有资金为" + bankAccountPreTruck.getMoney()
                            + "    可用余额为" + bankAccountPreTruck.getAvailableMoney());


//            cargo = preCargo;
            result = result + "承运方" + preCargo.getTruckId() + "向平台支付运费" + freightFare +
                    "\n平台向承运方" + cargo.getTruckId() + "支付酬劳" + String.format("%.2f", bidPrice) +
                    "\n平台向原转单承运方" + preCargo.getTruckId() +
                    "支付利润分配" + String.format("%.2f", truck1Profit) + "*" + rank1 +
                    "/10=" + String.format("%.2f", trueTruck1Profit) +
                    "\n平台向接单承运方" + cargo.getTruckId() +
                    "支付利润分配" + String.format("%.2f", truck2Profit) + "*" + rank2 +
                    "/10=" + String.format("%.2f", trueTruck2Profit) +
                    "平台该订单收益为" + (profitSpace - trueTruck1Profit - trueTruck2Profit) +
                    "\n";
        }

        /**
         * 非转单部分的资金结算
         * 此处为第一单承运方（没有转单则为唯一承运方）与发货方的结算；
         */
        else {
            result = result + "\n进入原始订单结算\n";
            double freightFare = cargo.getFreightFare();
            double bidPrice = cargo.getBidPrice();
            // 酬劳结算
            BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");

            // 1 首先前一个承运方支付运费prefare给平台
            logger.info("发货方" + cargo.getShipperId() + "向平台支付运费" + freightFare);
            // 2 然后平台向后一个承运方支付运费bidprice
            logger.info("平台向承运方" + cargo.getTruckId() + "支付酬劳" + bidPrice);
            // 3 计算利润空间
            double profitSpace = freightFare - bidPrice;
            // 4 计算各方分配的收益
            double paltformProfit = platformProfitRatio * profitSpace;
            double truck1Profit = shipperProfitRatio * profitSpace;
            double truck2Profit = truckProfitRatio * profitSpace;
            // 5 计算各方评级 以及真正的利润
            int rank1 = (int) shipperRepository.findShippersById(cargo.getShipperId()).getRanking();
            int rank2 = (int) truckRepository.findTruckById(cargo.getTruckId()).getRanking();

            double trueTruck1ProfitTemp = truck1Profit * rank1 * 0.1;
            double trueTruck2ProfitTemp = truck2Profit * rank2 * 0.1;

            double bonusMaxRatioInFare = platform.getBonusMaxRatioInFare();
            double maxbonux = bonusMaxRatioInFare * cargo.getFreightFare();

            // 6 获取利润分配资金与利润分配上限的最低值作为真正利润分配
            double trueTruck1Profit = trueTruck1ProfitTemp < maxbonux ? trueTruck1ProfitTemp : maxbonux;
            double trueTruck2Profit = trueTruck2ProfitTemp < maxbonux ? trueTruck2ProfitTemp : maxbonux;

            logger.info("平台向发货方" + cargo.getShipperId() + "支付利润分配" + truck1Profit);
            logger.info("平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit);

            result = result + "发货方" + cargo.getShipperId() + "向平台支付运费" + freightFare +
                    "\n平台向承运方" + cargo.getTruckId() + "支付酬劳" + String.format("%.2f", bidPrice) +
                    "\n平台向发货方" + cargo.getShipperId() +
                    "支付利润分配" + String.format("%.2f", truck1Profit) + "*" + rank1 +
                    "/10=" + String.format("%.2f", trueTruck1Profit) +
                    "\n平台向接单承运方" + cargo.getTruckId() +
                    "支付利润分配" + String.format("%.2f", truck2Profit) + "*" + rank2 +
                    "/10=" + String.format("%.2f", trueTruck2Profit) +
                    "平台该订单收益为" + (profitSpace - trueTruck1Profit - trueTruck2Profit) +
                    "\n";

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");
            bankAccountService.addMoneyLog(bankAccountShipper,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");
            bankAccountService.addMoneyLog(bankAccountTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的运输完成");

            // 完成订单后先解冻资金
            bankAccountService.changeAvailableMoney(bankAccountShipper, freightFare);
            bankAccountService.transferMoney(bankAccountShipper, bankAccountPlatform, freightFare);
            bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, bidPrice);

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配");
            bankAccountService.addMoneyLog(bankAccountTruck,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配，平台支付"
                            + String.format("%.2f", trueTruck2Profit) + "作为红包");
            bankAccountService.addMoneyLog(bankAccountShipper,
                    df.format(new Date()) + "  由于订单" + cargo.getId() + "的利润分配，"
                            + "发货方" + cargo.getShipperId() + "获得红包"
                            + String.format("%.2f", trueTruck1Profit));

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    "平台发给发货方" + cargo.getShipperId() + "红包" + trueTruck1Profit
                            + " 平台付给承运方" + cargo.getTruckId() + "红包" + trueTruck2Profit);


            webSocketTest3.sendToUser2(String.valueOf(bankAccountShipper.getId()), "3*" + String.valueOf(cargo.getId()) + "*" + String.valueOf(trueTruck1Profit));
            webSocketTest.sendToUser2(String.valueOf(bankAccountTruck.getId()), "6*" + String.valueOf(cargo.getId()) + "*" + String.valueOf(trueTruck2Profit));

            bonusService.getBonus(bankAccountShipper, trueTruck1Profit);
            bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, trueTruck2Profit);

            bankAccountService.addMoneyLog(bankAccountPlatform,
                    "------------------------------------\n" +
                            " 现有资金为" + bankAccountPlatform.getMoney()
                            + "    可用余额为" + bankAccountPlatform.getAvailableMoney());
            bankAccountService.addMoneyLog(bankAccountTruck,
                    "------------------------------------\n" +
                            " 现有资金为" + bankAccountTruck.getMoney()
                            + "    可用余额为" + bankAccountTruck.getAvailableMoney());
            bankAccountService.addMoneyLog(bankAccountShipper,
                    "------------------------------------\n" +
                            " 现有资金为" + bankAccountShipper.getMoney()
                            + "    可用余额为" + bankAccountShipper.getAvailableMoney()
                            + "    当前红包为" + bankAccountShipper.getBonus());

            cargoRepository.save(cargo);
        }

        return result;
    }
}
