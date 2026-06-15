package com.example.demo.controller;

import com.example.demo.dto.ReportDto;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public List<ReportDto> getAll() {
        return reportService.getAll();
    }

    @PostMapping("/generate")
    public ReportDto generate(
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return reportService.generate(type, from, to);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        reportService.delete(id);
    }
}