package com.pitayafruits.api.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class ServiceLogAspect {

    @Around("execution(* com.pitayafruits.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {

        long begin = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();
        String pointName = joinPoint.getTarget().getClass().getName()
                + "." + joinPoint.getSignature().getName();

        long end = System.currentTimeMillis();

        long takeTime = end - begin;

        if (takeTime > 3000) {
            log.error("{} 执行耗时：{} 毫秒", pointName, takeTime);
        } else if (takeTime > 2000) {
            log.warn("{} 执行耗时：{} 毫秒", pointName, takeTime);
        } else {
            log.info("{} 执行耗时：{} 毫秒", pointName, takeTime);
        }
        return proceed;
    }
}
