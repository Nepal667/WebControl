package com.example.demo.controller;

import com.example.demo.dto.PolicyDto;
import com.example.demo.dto.request.CreatePolicyRequest;
import com.example.demo.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public List<PolicyDto> getAll() {
        return policyService.getAll();
    }

    @GetMapping("/{id}")
    public PolicyDto getById(@PathVariable UUID id) {
        return policyService.getById(id);
    }

    @PostMapping
    public PolicyDto create(@RequestBody CreatePolicyRequest request) {
        return policyService.create(request);
    }

    @PutMapping("/{id}")
    public PolicyDto update(@PathVariable UUID id, @RequestBody CreatePolicyRequest request) {
        return policyService.update(id, request);
    }

    @PutMapping("/{id}/activate")
    public PolicyDto activate(@PathVariable UUID id) {
        return policyService.activate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}