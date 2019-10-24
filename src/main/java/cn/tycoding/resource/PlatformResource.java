package cn.tycoding.resource;

import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.Bid;
import cn.tycoding.domain.Platform;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/platform")

public class PlatformResource {

    private final Logger logger = LoggerFactory.getLogger(PlatformResource.class);

    private final RedisTemplate redisTemplate;
    private final BankAccountService bankAccountService;
    private final String yearKey = "lastYearIncome";
    private final String monKey = "lastMonIncome";
    private final String dayKey = "lastDayIncome";
    private final int accountId = 1;
    private final String accountType = "platform";
    //这里的index可以为负数，-1表示最右边的一个，
    private final long lastIndex = -1;

    @Autowired
    private cn.tycoding.service.PlatformService platformService;
    @Autowired
    private cn.tycoding.service.TruckService truckService;
    @Autowired
    private cn.tycoding.service.ShipperService shipperService;
    @Autowired
    private cn.tycoding.service.ReceiverService receiverService;

    public PlatformResource(RedisTemplate redisTemplate, BankAccountService bankAccountService) {
        this.redisTemplate = redisTemplate;
        this.bankAccountService = bankAccountService;
    }

    /**
     * 参数设置
     *
     * @param platform
     * @return
     */
    @MyLog(value = " 参数设置")
    @PostMapping
    public Platform setPlatformElem(@RequestBody Platform platform) {
        String result = "参数设置";
//        platformService.savePlatform(platform);
        logger.info(result);
        return platformService.savePlatform(platform);
    }

    /**
     * 查询平台属性
     *
     * @param
     * @return
     */
    @MyLog(value = " 查询平台属性")
    @GetMapping("/params")
    public Platform showPlatformPara() {
        logger.info("平台属性展示");
        return platformService.showPlatformPara();
    }

    /**
     * 查看当前车辆、货物等统计数据
     *
     * @param
     * @return
     */
    @MyLog(value = "查看当前车辆、货物等统计数据 ")
    @GetMapping("/data")
    public String showPlatformList() {
        logger.info("平台信息一览");
        return platformService.showPlatformList();
    }

    /**
     * 查看所有出价
     *
     * @param
     * @return
     */
    @MyLog(value = "查看所有出价 ")
    @GetMapping("/bid")
    public List<Bid> showAllBid() {
        logger.info("出价信息一览");
        return platformService.showAllBid();
    }


    /**
     * 获取平台当前账户余额
     *
     * @return
     */
    @MyLog(value = " 获取平台当前账户余额")
    @GetMapping("/crtAccount")
    public double getCrtAccount() {
        return bankAccountService.getAvailableMoney(accountId, accountType);
    }

    /**
     * 前端异步获取当年收入
     *
     * @return
     */
    @MyLog(value = " 前端异步获取当年收入")
    @GetMapping("/{time}")
    public double getCrtIncome(@PathVariable("time") String time) {
        double last, current;
        if (time.equals("year")) {
            //上一次收入
            last = (double) redisTemplate.opsForList().index(yearKey, lastIndex);
            //当前收入
            current = bankAccountService.getAvailableMoney(accountId, accountType);

        } else if (time.equals("mon")) {

            last = (double) redisTemplate.opsForList().index(monKey, lastIndex);
            current = bankAccountService.getAvailableMoney(accountId, accountType);
        } else {

            last = (double) redisTemplate.opsForList().index(dayKey, lastIndex);
            current = bankAccountService.getAvailableMoney(accountId, accountType);

        }

        return current - last;
    }

    /**
     * 前端异步获取当月收入
     *
     * @return
     */
    @MyLog(value = "前端异步获取当月收入 ")
    @GetMapping("/mon")
    public double getCrtMonIncome() {
        double last = (double) redisTemplate.opsForList().index(monKey, lastIndex);
        double current = bankAccountService.getAvailableMoney(accountId, accountType);
        return current - last;
    }

    /**
     * 前端异步获取当日收入
     *
     * @return
     */
    @MyLog(value = "前端异步获取当日收入 ")
    @GetMapping("/day")
    public double getCrtDayIncome() {
        double last = (double) redisTemplate.opsForList().index(dayKey, lastIndex);
        double current = bankAccountService.getAvailableMoney(accountId, accountType);
        return current - last;
    }


    /**
     * 注销，为节省接口空间，合并三方的注销
     *
     * @param id
     * @return
     */
    @MyLog(value = "注销 ")
    @DeleteMapping("/{userType}/{id}")
    public String deleteReceiver(@PathVariable("userType") String userType, @PathVariable("id") int id) {
        logger.info("注销请求");
        if (userType.equals("trucks")) {
            return truckService.deleteTruck(id);
        }
        if (userType.equals("shippers")) {
            return shipperService.deleteShipper(id);
        }
        if (userType.equals("receivers")) {
            return receiverService.deleteReceiver(id);
        }
        return "注销异常！";
    }

}
