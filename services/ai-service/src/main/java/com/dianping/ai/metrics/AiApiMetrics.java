package com.dianping.ai.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AiApiMetrics {

    private static final Logger logger = LoggerFactory.getLogger(AiApiMetrics.class);

    private static final String METRIC_PREFIX = "ai_api";
    public static final String CALLS_TOTAL = METRIC_PREFIX + "_calls_total";
    public static final String CALLS_SUCCESS = METRIC_PREFIX + "_calls_success";
    public static final String CALLS_FAILED = METRIC_PREFIX + "_calls_failed";
    private static final String LATENCY_SECONDS = METRIC_PREFIX + "_latency_seconds";
    public static final String TOKENS_CONSUMED = METRIC_PREFIX + "_tokens_consumed";
    public static final String COST_ESTIMATE = METRIC_PREFIX + "_cost_estimate";

    private static final double INPUT_COST_PER_1K = 0.004;
    private static final double OUTPUT_COST_PER_1K = 0.012;

    private final Counter callsTotal;
    private final Counter callsSuccess;
    private final Counter callsFailed;
    private final Timer latencyTimer;
    private final Counter tokensConsumed;
    private final AtomicLong dailyCost;

    public AiApiMetrics(MeterRegistry registry) {
        this.callsTotal = Counter.builder(CALLS_TOTAL)
                .description("Total number of AI API calls")
                .register(registry);

        this.callsSuccess = Counter.builder(CALLS_SUCCESS)
                .description("Number of successful AI API calls")
                .register(registry);

        this.callsFailed = Counter.builder(CALLS_FAILED)
                .description("Number of failed AI API calls")
                .register(registry);

        this.latencyTimer = Timer.builder(LATENCY_SECONDS)
                .description("AI API call latency in seconds")
                .register(registry);

        this.tokensConsumed = Counter.builder(TOKENS_CONSUMED)
                .description("Total number of tokens consumed")
                .register(registry);

        this.dailyCost = new AtomicLong(0);
        Gauge.builder(COST_ESTIMATE, dailyCost, AtomicLong::get)
                .description("Estimated daily AI API cost in CNY")
                .register(registry);

        logger.info("AI API Metrics initialized");
    }

    public void recordSuccess(long latencyMs, int inputTokens, int outputTokens) {
        callsTotal.increment();
        callsSuccess.increment();
        
        latencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
        
        int totalTokens = inputTokens + outputTokens;
        tokensConsumed.increment(totalTokens);
        
        double cost = (inputTokens / 1000.0) * INPUT_COST_PER_1K + (outputTokens / 1000.0) * OUTPUT_COST_PER_1K;
        dailyCost.addAndGet((long) (cost * 100));
        
        logger.info("AI API call succeeded - latency: {}ms, tokens: {}, estimated cost: {} CNY", 
                latencyMs, totalTokens, cost);
    }

    public void recordFailure(long latencyMs) {
        callsTotal.increment();
        callsFailed.increment();
        
        latencyTimer.record(latencyMs, TimeUnit.MILLISECONDS);
        
        logger.warn("AI API call failed - latency: {}ms", latencyMs);
    }

    public double getDailyCostCny() {
        return dailyCost.get() / 100.0;
    }

    public void resetDailyCost() {
        dailyCost.set(0);
        logger.info("Daily AI API cost counter reset");
    }

    public double getTotalCalls() {
        return callsTotal.count();
    }

    public double getSuccessRate() {
        double total = callsTotal.count();
        if (total == 0) {
            return 100.0;
        }
        return (callsSuccess.count() / total) * 100;
    }
}