package com.example.demo.service;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.dto.DnsLogDto;
import com.example.demo.model.DnsLog;
import com.example.demo.model.DnsStatus;
import com.example.demo.repository.DnsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DnsLogService {

    private final DnsLogRepository dnsLogRepository;

    public Page<DnsLogDto> getLogs(String status, String domain, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null && !status.isBlank()) {
            DnsStatus dnsStatus = DnsStatus.valueOf(status.toUpperCase());
            return dnsLogRepository.findByStatusOrderByTimestampDesc(dnsStatus.name(), pageable)
                .map(this::toDto);
        }

        if (domain != null && !domain.isBlank()) {
            return dnsLogRepository.findByDomainContainingIgnoreCaseOrderByTimestampDesc(domain, pageable)
                    .map(this::toDto);
        }

        return dnsLogRepository.findAllByOrderByTimestampDesc(pageable).map(this::toDto);
    }

    public DashboardStatsDto getStats() {
        long total = dnsLogRepository.count();
        long blocked = dnsLogRepository.countByStatus(DnsStatus.BLOCKED.name());
        long allowed = total - blocked;

        List<Object[]> topDomains = dnsLogRepository.findTopDomains(PageRequest.of(0, 5));
        List<Object[]> topBlocked = dnsLogRepository.findTopBlockedDomains(PageRequest.of(0, 5));

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalRequests(total);
        dto.setBlockedRequests(blocked);
        dto.setAllowedRequests(allowed);
        dto.setBlockingRate(total > 0 ? (double) blocked / total * 100 : 0);
        dto.setTopDomains(mapTopDomains(topDomains));
        dto.setTopBlockedDomains(mapTopDomains(topBlocked));
        return dto;
    }

    public DnsLogDto save(DnsLog log) {
        return toDto(dnsLogRepository.save(log));
    }

    private List<Map<String, Object>> mapTopDomains(List<Object[]> rows) {
        return rows.stream().map(row -> Map.of(
                "domain", row[0],
                "count", row[1]
        )).collect(Collectors.toList());
    }

    public DnsLogDto toDto(DnsLog log) {
        DnsLogDto dto = new DnsLogDto();
        dto.setId(log.getId());
        dto.setTimestamp(log.getTimestamp());
        dto.setDomain(log.getDomain());
        dto.setClientIp(log.getClientIp());
        dto.setStatus(log.getStatus().name());
        dto.setResponseTime(log.getResponseTime());
        return dto;
    }
}