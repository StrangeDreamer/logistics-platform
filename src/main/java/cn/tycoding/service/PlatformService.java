package cn.tycoding.service;


import cn.tycoding.domain.Platform;
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

    public Platform savePlatform(Platform platform){
        Platform platform1 = new Platform();
        platform1.setLowestBidPriceRatio(platform.getLowestBidPriceRatio());
        platform1.setShipperProfitRatio(platform.getShipperProfitRatio());
        platform1.setPlatformProfitRatio(platform.getPlatformProfitRatio());
        platform1.setTruckProfitRatio(platform.getTruckProfitRatio());
        platform1.setWithdrawFeeRatio(platform.getWithdrawFeeRatio());
        platform1.setOverTimeFeeRatio(platform.getOverTimeFeeRatio());
        platform1.setExibitionFee(platform.getExibitionFee());

        logger.info("保存平台参数设定");
        platformRepository.save(platform1);
        return platform1;
    }

}
