package com.example.demo.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BlockedDomainDto {
    private UUID id;
    private String domain;
    private UUID categoryId;
    private String categoryName;
    private UUID policyId;
    private OffsetDateTime createdAt;
}