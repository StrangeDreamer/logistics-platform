package cn.tycoding.scheduledTasks;

import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class ScheduledTasks1 {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    private Logger logger = LoggerFactory.getLogger(ScheduledTasks1.class);
    private final WebSocketTest webSocketTest;

    private int fixedDelayCount = 1;
    private int fixedRateCount = 1;
    private int initialDelayCount = 1;
    private int cronCount = 1;

    public ScheduledTasks1(WebSocketTest webSocketTest) {
        this.webSocketTest = webSocketTest;
    }

    @Scheduled(fixedDelay = 5000)        //fixedDelay = 5000表示当前方法执行完毕5000ms后，Spring scheduling会再次调用该方法
    public void testFixDelay() {
        webSocketTest.sendToUser2("1234", "===fixedDelay: 第" + fixedDelayCount + "次执行方法");
        logger.info("===fixedDelayCount: 第{}次执行方法", fixedDelayCount++);
    }

    @Scheduled(fixedRate = 5000)        //fixedRate = 5000表示当前方法开始执行5000ms后，Spring scheduling会再次调用该方法
    public void testFixedRate() {

        webSocketTest.sendToUser2("456", "===fixedRate: 第" + fixedRateCount + "次执行方法");
        logger.info("===fixedRateCount: 第{}次执行方法", fixedRateCount++);
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5000)   //initialDelay = 1000表示延迟1000ms执行第一次任务
    public void testInitialDelay() {
        webSocketTest.sendToUser2("789", "===initialDelay: 第" + initialDelayCount + "次执行方法");
        logger.info("===initialDelayCount: 第{}次执行方法", initialDelayCount++);
    }

    @Scheduled(cron = "0 0/1 * * * ?")  //cron接受cron表达式，根据cron表达式确定定时规则
    public void testCron() {
        logger.info("===initialDelay: 第{}次执行方法", cronCount++);
    }

}
