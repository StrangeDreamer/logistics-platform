package cn.tycoding.scheduledTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
@Component
public class ScheduledTasks1 {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


        private Logger logger = LoggerFactory.getLogger(ScheduledTasks1.class);

        private int fixedDelayCount = 1;
        private int fixedRateCount = 1;
        private int initialDelayCount = 1;
        private int cronCount = 1;

        @Scheduled(fixedDelay = 5000)        //fixedDelay = 5000表示当前方法执行完毕5000ms后，Spring scheduling会再次调用该方法
        public void testFixDelay() {
            logger.info("===fixedDelay: 第{}次执行方法", fixedDelayCount++);
        }

        @Scheduled(fixedRate = 5000)        //fixedRate = 5000表示当前方法开始执行5000ms后，Spring scheduling会再次调用该方法
        public void testFixedRate() {
            logger.info("===fixedRate: 第{}次执行方法", fixedRateCount++);
        }

        @Scheduled(initialDelay = 1000, fixedRate = 5000)   //initialDelay = 1000表示延迟1000ms执行第一次任务
        public void testInitialDelay() {
            logger.info("===initialDelay: 第{}次执行方法", initialDelayCount++);
        }

        @Scheduled(cron = "0 0/1 * * * ?")  //cron接受cron表达式，根据cron表达式确定定时规则
        public void testCron() {
            logger.info("===initialDelay: 第{}次执行方法", cronCount++);
        }

}
