package cn.tycoding.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD) //注解放置的目标位置,METHOD是可注解在方法级别上
@Retention(RetentionPolicy.RUNTIME) //注解在哪个阶段执行
@Documented //生成文档
public @interface MoneyLog {
    String  moneyReceiver() ;
    //金额,增加用+，减少用-
    String amount();
    //原因
    String moneyRemarks();


}
