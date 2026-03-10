package com.dianping.common.util;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流key前缀
     */
    String prefix() default "rate_limit:";
    
    /**
     * 限流key，支持SpEL表达式
     */
    String key() default "";
    
    /**
     * 每秒允许的请求数
     */
    double permitsPerSecond() default 10.0;
    
    /**
     * 突发容量
     */
    long burstCapacity() default 20;
    
    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后重试";
}
