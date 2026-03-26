package com.dianping.common.port;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.dto.GenerateReasonRequest;
import com.dianping.common.dto.GenerateReasonResponse;
import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "ai-service", path = "/api/ai")
public interface AiPort {

    @PostMapping("/audit")
    AuditResult audit(AuditRequest request);

    @PostMapping("/recommend")
    List<ShopDTO> recommend(RecommendRequest request);
    
    @PostMapping("/generate-reason")
    GenerateReasonResponse generateReason(GenerateReasonRequest request);
}
