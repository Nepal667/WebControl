package com.example.demo.repository;

import com.example.demo.model.BlockedDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockedDomainRepository extends JpaRepository<BlockedDomain, UUID> {
    Optional<BlockedDomain> findByDomain(String domain);
    List<BlockedDomain> findByCategoryId(UUID categoryId);
    boolean existsByDomain(String domain);
}