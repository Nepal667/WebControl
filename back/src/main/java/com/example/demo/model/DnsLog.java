package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "dns_log")
public class DnsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Column(nullable = false, length = 255)
    private String domain;

    @Column(name = "client_ip", nullable = false, columnDefinition = "inet")
    private String clientIp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DnsStatus status;

    @Column(name = "response_time")
    private Integer responseTime;
}