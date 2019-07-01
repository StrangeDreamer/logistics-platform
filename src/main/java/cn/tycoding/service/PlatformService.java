package cn.tycoding.service;


import cn.tycoding.domain.Platform;
import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.TransferredCargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-06-17 20:54
 * 4
 */

@Service
@Transactional
public class PlatformService {

    private final Logger logger = LoggerFactory.getLogger(InspectionService.class);
    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private  CargoRepository cargoRepository;

    @Autowired
    private  ShipperRepository shipperRepository;

    @Autowired
    private  TruckRepository truckRepository;

    @Autowired
    private  ReceiverRepository receiverRepository;

    public Platform savePlatform(Platform platform){
        Platform platform1 = new Platform();
        platform1.setLowestBidPriceRatio(platform.getLowestBidPriceRatio());
        platform1.setShipperProfitRatio(platform.getShipperProfitRatio());
        platform1.setPlatformProfitRatio(platform.getPlatformProfitRatio());
        platform1.setTruckProfitRatio(platform.getTruckProfitRatio());
        platform1.setWithdrawFeeRatio(platform.getWithdrawFeeRatio());
        platform1.setOverTimeFeeRatio(platform.getOverTimeFeeRatio());
        platform1.setExhibitionFee(platform.getExhibitionFee());
        platform1.setBidingDuration(platform.getBidingDuration());

        platform1.setBonusMaxRatioInFare(platform.getBonusMaxRatioInFare());
        logger.info("保存平台参数设定");
        platformRepository.save(platform1);
        return platform1;
    }


    public Platform showPlatformPara(){
        return platformRepository.findRecentPltf();
    }

    public String showPlatformList(){
//        List<Receiver> receiverList = receiverRepository.findAll();
        int shipperNum = shipperRepository.countCargosByIdIsNotNull();

        int truckNum = truckRepository.countCargosByIdIsNotNull();
        int receiverNum = receiverRepository.countCargosByIdIsNotNull();
        int cargoPublishingNum = cargoRepository.countAllByStatus(1);
        int cargoNum1 = cargoRepository.countAllByStatus(2);
        int cargoNum2 = cargoRepository.countAllByStatus(3);
        int cargoNum3 = cargoRepository.countAllByStatus(8);

        return "当前平台共有发货方" + shipperNum + "个，承运方" + truckNum +
                " 个，收货方" + receiverNum + "个,当前正在发布订单" + cargoPublishingNum +
                "个，当前正在执行的订单" + (cargoNum1 + cargoNum2) + "个，已完成订单" + cargoNum3 +"个" ;
    }

}
