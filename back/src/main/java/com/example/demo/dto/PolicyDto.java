package com.example.demo.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class PolicyDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private Set<CategoryDto> categories;
}