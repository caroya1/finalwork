package com.dianping.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 埋点SDK - 用户行为数据采集
 */
@Component
public class TrackingSDK {

    private final Map<String, EventType> eventTypeMap = new ConcurrentHashMap<>();
    
    public enum EventType {
        PAGE_VIEW("page_view", "页面浏览"),
        CLICK("click", "点击事件"),
        SEARCH("search", "搜索行为"),
        FAVORITE("favorite", "收藏行为"),
        ORDER("order", "下单行为"),
        PAYMENT("payment", "支付行为"),
        REVIEW("review", "评价行为");
        
        private final String code;
        private final String desc;
        
        EventType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() { return code; }
        public String getDesc() { return desc; }
    }
    
    public static class TrackEvent {
        private String eventId;
        private String eventType;
        private Long userId;
        private String sessionId;
        private String pageName;
        private String elementId;
        private Map<String, Object> properties;
        private LocalDateTime timestamp;
        
        public TrackEvent() {
            this.timestamp = LocalDateTime.now();
            this.eventId = UUID.randomUUID().toString();
        }
        
        public String getEventId() { return eventId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getPageName() { return pageName; }
        public void setPageName(String pageName) { this.pageName = pageName; }
        public String getElementId() { return elementId; }
        public void setElementId(String elementId) { this.elementId = elementId; }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("eventId", eventId);
            map.put("eventType", eventType);
            map.put("userId", userId);
            map.put("sessionId", sessionId);
            map.put("pageName", pageName);
            map.put("elementId", elementId);
            map.put("properties", properties);
            map.put("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return map;
        }
    }
    
    public TrackEvent trackPageView(String pageName) {
        TrackEvent event = new TrackEvent();
        event.setEventType(EventType.PAGE_VIEW.getCode());
        event.setPageName(pageName);
        return event;
    }
    
    public TrackEvent trackClick(String pageName, String elementId, String elementName) {
        TrackEvent event = new TrackEvent();
        event.setEventType(EventType.CLICK.getCode());
        event.setPageName(pageName);
        event.setElementId(elementId);
        return event;
    }
    
    public TrackEvent trackSearch(String keyword, Integer resultCount) {
        TrackEvent event = new TrackEvent();
        event.setEventType(EventType.SEARCH.getCode());
        Map<String, Object> props = new HashMap<>();
        props.put("keyword", keyword);
        props.put("resultCount", resultCount);
        event.setProperties(props);
        return event;
    }
    
    public TrackEvent trackOrder(Long orderId, Long shopId, Integer amount) {
        TrackEvent event = new TrackEvent();
        event.setEventType(EventType.ORDER.getCode());
        Map<String, Object> props = new HashMap<>();
        props.put("orderId", orderId);
        props.put("shopId", shopId);
        props.put("amount", amount);
        event.setProperties(props);
        return event;
    }
}
