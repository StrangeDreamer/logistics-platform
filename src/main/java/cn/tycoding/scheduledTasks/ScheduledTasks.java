package cn.tycoding.scheduledTasks;

import cn.tycoding.domain.Platform;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.service.BankAccountService;
import cn.tycoding.service.PlatformService;
import cn.tycoding.websocket.WebSocketTest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
@Slf4j
public class ScheduledTasks {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final BankAccountService bankAccountService;
    private final int accountId=1;
    private final String accountType= "platform";
    private final RedisTemplate redisTemplate;
    private final String yearKey="lastYearIncome";
    private final String monKey="lastMonIncome";
    private final String dayKey="lastDayIncome";
    @Autowired
    private PlatformService platformService ;
    @Autowired
    private PlatformRepository platformRepository;



    public ScheduledTasks(BankAccountService bankAccountService, RedisTemplate redisTemplate) {

        this.bankAccountService = bankAccountService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * fixedDelay = 24000表示当前方法执行完毕24000ms后，Spring scheduling会再次调用该方法
     */
    @Scheduled(fixedDelay = 24000)
    public void getAccountLogDay() {
         double nowMoney=bankAccountService.getAvailableMoney(accountId,accountType);
        redisTemplate.opsForList().rightPush(dayKey,nowMoney);
        log.info("redis dayincome refresh");
    }

    @Scheduled(fixedDelay = 720000)
    public void getAccountLogMon() {
        double nowMoney=bankAccountService.getAvailableMoney(accountId,accountType);
        redisTemplate.opsForList().rightPush(monKey,nowMoney);
        log.info("redis monincome refresh");
    }

    @Scheduled(fixedDelay = 8640000)
    public void getAccountLogYear() {
        double nowMoney=bankAccountService.getAvailableMoney(accountId,accountType);
        redisTemplate.opsForList().rightPush(yearKey,nowMoney);
        log.info("redis yearincome refresh");
    }

    @Scheduled(fixedDelay = 24000)
    public void autoSetPlatformPara() {
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
