package cn.tycoding.aop;

import cn.tycoding.domain.FundsLog;
import cn.tycoding.service.FundsLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
@Component
public class MoneyLogAspect {


    @Autowired
    private FundsLogService fundsLogService;


    //定义切点 @Pointcut
    //在注解的位置切入代码
    @Pointcut("@annotation( cn.tycoding.aop.MoneyLog)")
    public void logPoinCut() {
    }

    //切面 配置通知
    @AfterReturning("logPoinCut()")
    public void saveMoneyLog(JoinPoint joinPoint) {
        //保存日志
        FundsLog fundsLog=new FundsLog();

        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();

        //获取操作
        MoneyLog moneyLog = method.getAnnotation(MoneyLog.class);
        if (moneyLog != null) {
            String moneyReceiver = moneyLog.moneyReceiver();
            String amount=moneyLog.amount();
            String moneyRemarks=moneyLog.moneyRemarks();
            fundsLog.setAmount(amount);
            fundsLog.setMoneyReceiver(moneyReceiver);
            fundsLog.setMoneyRemarks(moneyRemarks);
        }
        //调用service保存SysLog实体类到数据库
        fundsLogService.save(fundsLog);
    }
}
