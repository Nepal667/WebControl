package com.example.demo.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateBlockedDomainRequest {
    private String domain;
    private UUID categoryId;
    private UUID policyId;
}