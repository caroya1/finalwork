package com.dianping.recommendation.repository;

import com.dianping.recommendation.entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {
}
