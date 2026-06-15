package com.example.demo.repository;

import com.example.demo.model.DnsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;

public interface DnsLogRepository extends JpaRepository<DnsLog, Long> {

    // ── Remplacé : native query avec cast explicite ──
    @Query(value = "SELECT COUNT(*) FROM dns_log WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM dns_log WHERE status = :status ORDER BY timestamp DESC",
        countQuery = "SELECT COUNT(*) FROM dns_log WHERE status = :status",
        nativeQuery = true)
    Page<DnsLog> findByStatusOrderByTimestampDesc(@Param("status") String status, Pageable pageable);

    // ── Inchangées ──
    Page<DnsLog> findByDomainContainingIgnoreCaseOrderByTimestampDesc(String domain, Pageable pageable);

    Page<DnsLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT d.domain, COUNT(d) as total FROM DnsLog d GROUP BY d.domain ORDER BY total DESC")
    List<Object[]> findTopDomains(Pageable pageable);

    @Query("SELECT d.domain, COUNT(d) as total FROM DnsLog d WHERE d.status = 'BLOCKED' GROUP BY d.domain ORDER BY total DESC")
    List<Object[]> findTopBlockedDomains(Pageable pageable);

    List<DnsLog> findByTimestampBetween(OffsetDateTime from, OffsetDateTime to);

    long countByTimestampAfter(OffsetDateTime from);
}