package com.dianping.common.util;

import com.dianping.common.exception.BusinessException;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面（基于Guava RateLimiter）
 */
@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateKey(point, rateLimit);
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> createLimiter(rateLimit));
        
        if (limiter.tryAcquire()) {
            return point.proceed();
        } else {
            throw new BusinessException(rateLimit.message());
        }
    }

    private RateLimiter createLimiter(RateLimit rateLimit) {
        return RateLimiter.create(rateLimit.permitsPerSecond());
    }

    private String generateKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder(rateLimit.prefix());
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        // 添加方法名
        key.append(method.getDeclaringClass().getName()).append(".").append(method.getName());
        
        // 解析SpEL表达式
        String spelKey = rateLimit.key();
        if (!spelKey.isEmpty()) {
            key.append(":").append(parseSpEL(spelKey, method, point.getArgs()));
        }
        
        // 添加IP地址
        key.append(":").append(getClientIp());
        
        return key.toString();
    }

    private String parseSpEL(String spel, Method method, Object[] args) {
        try {
            Expression expression = parser.parseExpression(spel);
            EvaluationContext context = new StandardEvaluationContext();
            
            // 添加方法参数
            String[] paramNames = discoverer.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return spel;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip != null ? ip.split(",")[0].trim() : "unknown";
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }
}
