package cn.tycoding.aop;

import cn.tycoding.Utils.*;
import cn.tycoding.domain.SysLog;
import cn.tycoding.service.SysLogService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 系统日志：切面处理类
 */
@Aspect
@Component
public class SysLogAspect {

    @Autowired
    private SysLogService sysLogService;


    //定义切点 @Pointcut
    //在注解的位置切入代码
    @Pointcut("@annotation( cn.tycoding.aop.MyLog)")
    public void logPoinCut() {
    }

    //切面 配置通知
    @AfterReturning("logPoinCut()")
    public void saveSysLog(JoinPoint joinPoint) {
        //保存日志
        SysLog sysLog = new SysLog();

        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();

        //获取操作
        MyLog myLog = method.getAnnotation(MyLog.class);
        if (myLog != null) {
            String value = myLog.value();
            sysLog.setOperation(value);//保存获取的操作
        }

        //获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();
        //获取请求的方法名
        String methodName = method.getName();
        sysLog.setMethod(className + "." + methodName);

        //请求的参数
        Object[] args = joinPoint.getArgs();
        //将参数所在的数组转换成json
        String params = JSON.toJSONString(args, SerializerFeature.IgnoreNonFieldGetter);
        sysLog.setParams(params);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sysLog.setCreateDate(df.format(new Date()));
        //获取用户名
        sysLog.setUsername(CurrentUserUtils.getCurrentUsername());

        //获取用户ip地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        sysLog.setIp(IpAddressUtil.getIpAddress(request));

        //获取用户请求url
        sysLog.setUri(request.getRequestURI());

        //调用service保存SysLog实体类到数据库
        sysLogService.save(sysLog);
    }

}
