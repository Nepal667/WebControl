package com.example.demo.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReportDto {
    private UUID id;
    private OffsetDateTime generatedAt;
    private String type;
    private String filePath;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
}