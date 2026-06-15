package com.example.demo.service;

import com.example.demo.model.DnsLog;
import com.example.demo.model.DnsStatus;
import com.example.demo.repository.DnsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DnsLogPoller {

    private final PiHoleClient piHoleClient;
    private final DnsLogRepository dnsLogRepository;

    private OffsetDateTime lastPolled = OffsetDateTime.now().minusMinutes(5);

    @Scheduled(fixedDelay = 30000)
    public void pollAndSave() {
        try {
            List<Map<String, Object>> queries = piHoleClient.getRecentQueries();
            if (queries == null || queries.isEmpty()) return;

            int saved = 0;

            for (Map<String, Object> query : queries) {
                try {
                    Object timeObj = query.get("time");
                    if (timeObj == null) continue;

                    // Pi-hole retourne parfois en notation scientifique
                    double epochDouble = Double.parseDouble(timeObj.toString());
                    long epochSeconds = (long) epochDouble;

                    OffsetDateTime timestamp = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(epochSeconds),
                        ZoneId.systemDefault()
                    );

                    if (!timestamp.isAfter(lastPolled)) continue;

                    String domain   = String.valueOf(query.getOrDefault("domain", "unknown"));
                    String clientIp = String.valueOf(query.getOrDefault("client", "0.0.0.0"));

                    Object statusObj = query.get("status");
                    DnsStatus status = DnsStatus.ALLOWED;
                    if (statusObj != null) {
                         String statusStr = statusObj.toString().toUpperCase();
                        if (statusStr.equals("GRAVITY") || statusStr.equals("REGEX")
                                || statusStr.equals("DENYLIST") || statusStr.equals("EXTERNAL_BLOCKED_IP")
                                || statusStr.equals("EXTERNAL_BLOCKED_NULL") || statusStr.equals("EXTERNAL_BLOCKED_NXRA")) {
                            status = DnsStatus.BLOCKED;
                        }
                    }

                    DnsLog log = new DnsLog();
                    log.setTimestamp(timestamp);
                    log.setDomain(domain);
                    log.setClientIp(clientIp);
                    log.setStatus(status);

                    dnsLogRepository.save(log);
                    saved++;

                } catch (Exception e) {
                    System.err.println("Erreur parsing query : " + e.getMessage());
                }
            }

            lastPolled = OffsetDateTime.now();
            System.out.println("Pi-hole poll OK — " + saved + " nouveaux logs sauvegardés");

        } catch (Exception e) {
            System.err.println("Erreur poll Pi-hole : " + e.getMessage());
        }
    }
}