package io.datavines.server.coordinator.api.aop;

import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.utils.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Slf4j
@Component
@Order(Integer.MAX_VALUE)
public class RefreshTokenAop {

    @Resource
    private TokenManager tokenManager;

    @Pointcut("@within(RefreshToken)")
    public void pointCut() {

    }

    @Around(value = "pointCut()")
    public Object doAroundReturningAdvice(ProceedingJoinPoint point) throws Throwable{
        Object[] args = point.getArgs();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Object result = point.proceed(args);

        return ResponseEntity.ok(new ResultMap(tokenManager).successAndRefreshToken(request).payload(result));
    }
}
