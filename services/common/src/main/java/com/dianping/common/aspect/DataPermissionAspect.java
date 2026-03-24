package com.dianping.common.aspect;

import com.dianping.common.annotation.DataPermission;
import com.dianping.common.annotation.PermissionType;
import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 数据权限切面
 * 拦截@DataPermission注解，进行数据权限校验
 */
@Aspect
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class DataPermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionAspect.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String PERMISSION_CACHE_PREFIX = "dp:permission:";
    private static final long PERMISSION_CACHE_TTL_MINUTES = 5;

    @Around("@annotation(dataPermission)")
    public Object around(ProceedingJoinPoint point, DataPermission dataPermission) throws Throwable {
        
        UserSession session = UserContext.get();
        if (session == null) {
            throw new AccessDeniedException("未登录");
        }

        // 1. 检查缓存
        String cacheKey = buildCacheKey(point, session, dataPermission);
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            boolean cachedPermission = Boolean.parseBoolean(cachedResult);
            if (!cachedPermission) {
                throw new AccessDeniedException("无权访问此资源（缓存）");
            }
            return point.proceed();
        }

        // 2. 执行权限校验
        boolean hasPermission = checkPermission(point, dataPermission, session);
        
        // 3. 写入缓存
        redisTemplate.opsForValue().set(
            cacheKey, 
            String.valueOf(hasPermission), 
            PERMISSION_CACHE_TTL_MINUTES, 
            TimeUnit.MINUTES
        );

        if (!hasPermission) {
            log.warn("数据权限校验失败: userId={}, type={}, entity={}", 
                session.getId(), dataPermission.type(), dataPermission.entityType());
            throw new AccessDeniedException("无权访问此资源");
        }

        return point.proceed();
    }

    private boolean checkPermission(ProceedingJoinPoint point, 
                                    DataPermission dataPermission, 
                                    UserSession session) {
        switch (dataPermission.type()) {
            case PUBLIC:
                return true;
                
            case ADMIN_ONLY:
                return session.isAdmin();
                
            case OWNER_ONLY:
                // 从参数中提取用户ID
                Long resourceUserId = extractResourceId(point, dataPermission.resourceId());
                return session.getId().equals(resourceUserId);
                
            case MERCHANT_OWNED:
                // 商户只能访问自己的数据
                if (!session.isMerchant() || session.getMerchantId() == null) {
                    return false;
                }
                // 这里需要查询资源的商户ID是否匹配
                // 简化处理：假设资源ID就是商户ID，实际应该查询数据库
                Long resourceId = extractResourceId(point, dataPermission.resourceId());
                return session.getMerchantId().equals(resourceId);
                
            default:
                return false;
        }
    }

    private Long extractResourceId(ProceedingJoinPoint point, String resourceIdName) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        String[] paramNames = signature.getParameterNames();
        
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(resourceIdName) && args[i] instanceof Number) {
                return ((Number) args[i]).longValue();
            }
        }
        
        // 默认返回第一个Long类型的参数
        for (Object arg : args) {
            if (arg instanceof Number) {
                return ((Number) arg).longValue();
            }
        }
        
        return null;
    }

    private String buildCacheKey(ProceedingJoinPoint point, 
                                  UserSession session, 
                                  DataPermission dataPermission) {
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();
        Long resourceId = extractResourceId(args, dataPermission.resourceId());
        
        return String.format("%s%s:%s:%s:%s", 
            PERMISSION_CACHE_PREFIX,
            session.getId(),
            dataPermission.type(),
            dataPermission.entityType(),
            resourceId);
    }

    private Long extractResourceId(Object[] args, String resourceIdName) {
        for (Object arg : args) {
            if (arg instanceof Number) {
                return ((Number) arg).longValue();
            }
        }
        return null;
    }
}
