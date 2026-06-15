package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.dto.DnsLogDto;
import com.example.demo.service.DnsLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DnsLogController {

    private final DnsLogService dnsLogService;

    @GetMapping
    public Page<DnsLogDto> getLogs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return dnsLogService.getLogs(status, domain, page, size);
    }

    @GetMapping("/stats")
    public DashboardStatsDto getStats() {
        return dnsLogService.getStats();
    }
}