package com.example.demo.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsDto {
    private long totalRequests;
    private long blockedRequests;
    private long allowedRequests;
    private double blockingRate;
    private List<Map<String, Object>> topDomains;
    private List<Map<String, Object>> topBlockedDomains;
}