package com.example.demo.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;
}