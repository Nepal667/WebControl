package com.example.demo.service;

import com.example.demo.dto.BlockedDomainDto;
import com.example.demo.dto.request.CreateBlockedDomainRequest;
import com.example.demo.model.BlockedDomain;
import com.example.demo.model.Category;
import com.example.demo.model.Policy;
import com.example.demo.repository.BlockedDomainRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockedDomainService {

    private final PiHoleClient piHoleClient;
    private final BlockedDomainRepository blockedDomainRepository;
    private final CategoryRepository categoryRepository;
    private final PolicyRepository policyRepository;

    public List<BlockedDomainDto> getAll() {
        return blockedDomainRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public BlockedDomainDto create(CreateBlockedDomainRequest request) {
        if (blockedDomainRepository.existsByDomain(request.getDomain())) {
            throw new RuntimeException("Domaine déjà bloqué : " + request.getDomain());
        }

        BlockedDomain domain = new BlockedDomain();
        domain.setDomain(request.getDomain());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            domain.setCategory(category);
        }

        if (request.getPolicyId() != null) {
            Policy policy = policyRepository.findById(request.getPolicyId())
                    .orElseThrow(() -> new RuntimeException("Politique introuvable"));
            domain.setPolicy(policy);
        }

        BlockedDomainDto saved = toDto(blockedDomainRepository.save(domain));

        // Synchronisation Pi-hole
        boolean ok = piHoleClient.blockDomain(request.getDomain());
        if (!ok) {
            System.err.println("Avertissement : domaine sauvegardé en base mais non synchronisé avec Pi-hole : " + request.getDomain());
        }

        return saved;
    }

    public void importDomains(List<String> domains, UUID categoryId, UUID policyId) {
        Category category = categoryId != null ? categoryRepository.findById(categoryId).orElse(null) : null;
        Policy policy = policyId != null ? policyRepository.findById(policyId).orElse(null) : null;

        domains.stream()
                .filter(d -> d != null && !d.isBlank())
                .filter(d -> !blockedDomainRepository.existsByDomain(d.trim()))
                .forEach(d -> {
                    BlockedDomain bd = new BlockedDomain();
                    bd.setDomain(d.trim());
                    bd.setCategory(category);
                    bd.setPolicy(policy);
                    blockedDomainRepository.save(bd);
                    piHoleClient.blockDomain(d.trim());
                });
    }

    public void delete(UUID id) {
        blockedDomainRepository.findById(id).ifPresent(bd -> {
            piHoleClient.unblockDomain(bd.getDomain());
            blockedDomainRepository.deleteById(id);
        });
    }

    public BlockedDomainDto toDto(BlockedDomain bd) {
        BlockedDomainDto dto = new BlockedDomainDto();
        dto.setId(bd.getId());
        dto.setDomain(bd.getDomain());
        dto.setCreatedAt(bd.getCreatedAt());
        if (bd.getCategory() != null) {
            dto.setCategoryId(bd.getCategory().getId());
            dto.setCategoryName(bd.getCategory().getName());
        }
        if (bd.getPolicy() != null) {
            dto.setPolicyId(bd.getPolicy().getId());
        }
        return dto;
    }
}