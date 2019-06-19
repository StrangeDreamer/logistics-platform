package cn.tycoding.service;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Platform;
import cn.tycoding.exception.InspectionException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.InspectionRepository;
import cn.tycoding.repository.PlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public String saveInspection(Inspection inspection){
        Inspection inspection1=new Inspection();
        inspection1.setCargoId(inspection.getCargoId());
        inspection1.setInspectionResult(inspection.getInspectionResult());
        logger.info("保存验货请求");
        inspectionRepository.save(inspection1);
        return "保存验货请求成功";
    }

    public String inspectionCargo(Inspection inspection){

        Platform platform = platformRepository.findRecentPltf();

        double overTimeFeeRatio = platform.getOverTimeFeeRatio();
        double platformProfitRatio = platform.getPlatformProfitRatio();
        double shipperProfitRatio = platform.getShipperProfitRatio();
        double truckProfitRatio = platform.getTruckProfitRatio();

        String result = "验货结果：";

        Cargo cargo = cargoRepository.findCargoById(inspection.getCargoId());

        if (cargo.getStatus() != 4) {
            logger.info("货物当前状态不允许验货！");
            throw new InspectionException("货物当前状态不允许验货！");
        }

        cargo.setStatus(inspection.getInspectionResult());
        cargoRepository.save(cargo);

//     * 8 正常完成
//     * 9 订单超时
//     * 10 订单异常
        // 验货异常/不通过直接抛出
        if (inspection.getInspectionResult() == 10) {
            result = "货物出现异常，交给第三方处理";
        } else if(inspection.getInspectionResult() != 8 || inspection.getInspectionResult() != 9) {
            logger.info("验货结果设置错误！");
            throw new InspectionException("验货结果设置错误！");
        }

        // 如果超时，则先结算超时赔偿
        if (inspection.getInspectionResult() == 9) {
            double compensation = cargo.getInsurance() * (overTimeFeeRatio + inspection.getTimeoutPeriod() * 0.01);
            result = "验货正常但出现超时！\n" +
                    "超时时长为：" + inspection.getTimeoutPeriod() + "承运方" + cargo.getTruckId()  +"需要支付赔偿金：" +
                    compensation + ";承运方" + cargo.getTruckId() + "的担保额度恢复仅当超时赔偿金支付完成后恢复！" ;
        }


        // 不停结算，直到追溯到发货方；此处的均为承运方与承运方之间的资金流动
        while (cargo.getPreCargoId() != null) {

            double preFare = cargo.getPreFare();
            double freightFare = cargo.getFreightFare();
            double bidPrice = cargo.getBidPrice();
            Cargo preCargo = cargoRepository.findCargoById(cargo.getPreCargoId());

            // TODO: 转单酬劳结算
            // 1 首先前一个承运方支付运费prefare给平台
            logger.info("承运方" + preCargo.getTruckId() + "向平台支付运费" + freightFare);
            // 2 然后平台向后一个承运方支付运费bidprice
            logger.info("平台向承运方" + cargo.getTruckId() +"支付酬劳" + bidPrice);
            // 3 计算利润空间
            double profitSpace = freightFare - bidPrice;
            // 4 计算各方分配的收益
            double paltformProfit = platformProfitRatio * profitSpace;
            double truck1Profit = shipperProfitRatio * profitSpace;
            double truck2Profit = truckProfitRatio * profitSpace;
            logger.info("平台向原转单承运方" + preCargo.getTruckId() + "支付利润分配" + truck1Profit);
            logger.info("平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit);
            cargo = preCargo;
            result = result +  "承运方" + preCargo.getTruckId() + "向平台支付运费" + freightFare +
                    "平台向承运方" + cargo.getTruckId() +"支付酬劳" + bidPrice +
                    "平台向原转单承运方" + preCargo.getTruckId() + "支付利润分配" + truck1Profit +
                    "平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit + "   \n  "
            ;
        }

        // 此处为第一单承运方（没有转单则为唯一承运方）与发货方的结算；
        // TODO 酬劳结算

        double freightFare = cargo.getFreightFare();
        double bidPrice = cargo.getBidPrice();

        // 1 首先前一个承运方支付运费prefare给平台
        logger.info("发货方" + cargo.getShipperId() + "向平台支付运费" + freightFare);
        // 2 然后平台向后一个承运方支付运费bidprice
        logger.info("平台向承运方" + cargo.getTruckId() +"支付酬劳" + bidPrice);
        // 3 计算利润空间
        double profitSpace = freightFare - bidPrice;
        // 4 计算各方分配的收益
        double paltformProfit = platformProfitRatio * profitSpace;
        double truck1Profit = shipperProfitRatio * profitSpace;
        double truck2Profit = truckProfitRatio * profitSpace;
        logger.info("平台向发货方" + cargo.getShipperId() + "支付利润分配" + truck1Profit);
        logger.info("平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit);

        result = result +  "承运方" + cargo.getShipperId() + "向平台支付运费" + freightFare +
                "平台向承运方" + cargo.getTruckId() +"支付酬劳" + bidPrice +
                "平台向原转单承运方" + cargo.getShipperId() + "支付利润分配" + truck1Profit +
                "平台向接单承运方" + cargo.getTruckId() + "支付利润分配" + truck2Profit + "   \n  "
        ;


        cargoRepository.save(cargo);
        return result;
    }

}
