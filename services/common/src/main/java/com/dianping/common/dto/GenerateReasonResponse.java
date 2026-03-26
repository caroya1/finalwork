package com.dianping.common.dto;

import java.util.List;

/**
 * AI生成推荐理由响应
 */
public class GenerateReasonResponse {
    
    private String recommendReason;
    private List<String> highlights;
    
    public GenerateReasonResponse() {
    }
    
    public GenerateReasonResponse(String recommendReason, List<String> highlights) {
        this.recommendReason = recommendReason;
        this.highlights = highlights;
    }
    
    public String getRecommendReason() {
        return recommendReason;
    }
    
    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }
    
    public List<String> getHighlights() {
        return highlights;
    }
    
    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }
}
