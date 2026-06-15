package com.example.demo.dto.request;

import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class CreatePolicyRequest {
    private String name;
    private String description;
    private Set<UUID> categoryIds;
}