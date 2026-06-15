package com.example.demo.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class DnsLogDto {
    private Long id;
    private OffsetDateTime timestamp;
    private String domain;
    private String clientIp;
    private String status;
    private Integer responseTime;
}