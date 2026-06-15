package com.example.demo.service;

import com.example.demo.dto.ReportDto;
import com.example.demo.model.Report;
import com.example.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<ReportDto> getAll() {
        return reportRepository.findAllByOrderByGeneratedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReportDto generate(String type, OffsetDateTime from, OffsetDateTime to) {
        Report report = new Report();
        report.setType(type);
        report.setPeriodStart(from);
        report.setPeriodEnd(to);
        report.setGeneratedAt(OffsetDateTime.now());
        return toDto(reportRepository.save(report));
    }

    public void delete(UUID id) {
        reportRepository.deleteById(id);
    }

    public ReportDto toDto(Report report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setType(report.getType());
        dto.setFilePath(report.getFilePath());
        dto.setPeriodStart(report.getPeriodStart());
        dto.setPeriodEnd(report.getPeriodEnd());
        return dto;
    }
}