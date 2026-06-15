package com.example.demo.service;

import com.example.demo.dto.PolicyDto;
import com.example.demo.dto.request.CreatePolicyRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Policy;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public List<PolicyDto> getAll() {
        return policyRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public PolicyDto getById(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique introuvable : " + id));
        return toDto(policy);
    }

    public PolicyDto create(CreatePolicyRequest request) {
        Policy policy = new Policy();
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setIsActive(false);
        if (request.getCategoryIds() != null) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(cid -> categoryRepository.findById(cid)
                            .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + cid)))
                    .collect(Collectors.toSet());
            policy.setCategories(categories);
        }
        return toDto(policyRepository.save(policy));
    }

    public PolicyDto update(UUID id, CreatePolicyRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique introuvable : " + id));
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        if (request.getCategoryIds() != null) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(cid -> categoryRepository.findById(cid)
                            .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + cid)))
                    .collect(Collectors.toSet());
            policy.setCategories(categories);
        }
        return toDto(policyRepository.save(policy));
    }

    @Transactional
    public PolicyDto activate(UUID id) {
        policyRepository.findByIsActiveTrue().ifPresent(active -> {
            active.setIsActive(false);
            policyRepository.save(active);
        });
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Politique introuvable : " + id));
        policy.setIsActive(true);
        return toDto(policyRepository.save(policy));
    }

    public void delete(UUID id) {
        policyRepository.deleteById(id);
    }

    public PolicyDto toDto(Policy policy) {
        PolicyDto dto = new PolicyDto();
        dto.setId(policy.getId());
        dto.setName(policy.getName());
        dto.setDescription(policy.getDescription());
        dto.setIsActive(policy.getIsActive());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setCategories(policy.getCategories().stream()
                .map(categoryService::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }
}