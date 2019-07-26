package cn.tycoding.scheduledTasks;

import cn.tycoding.domain.Platform;
import cn.tycoding.repository.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 自动设置平台参数
 * https://blog.csdn.net/rui15111/article/details/80996342
 */
@Component
@Order(value = 1)
@Slf4j
public class autoSetPlatformPara  implements ApplicationRunner {

    @Autowired
    private PlatformRepository platformRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (platformRepository.findRecentPltf() == null){
            Platform platform = new Platform();
            platform.setLowestBidPriceRatio(0.4);
            platform.setShipperProfitRatio(0.2);
            platform.setPlatformProfitRatio(0.3);
            platform.setTruckProfitRatio(0.5);
            platform.setWithdrawFeeRatio(0.2);
            platform.setOverTimeFeeRatio(0.05);
            platform.setExhibitionFee(10);
            platform.setBidingDuration(25);
            platformRepository.save(platform);
        }

        log.info("自动设置平台参数");
    }
}
