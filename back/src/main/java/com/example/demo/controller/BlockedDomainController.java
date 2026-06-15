package com.example.demo.controller;

import com.example.demo.dto.BlockedDomainDto;
import com.example.demo.dto.request.CreateBlockedDomainRequest;
import com.example.demo.service.BlockedDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlockedDomainController {

    private final BlockedDomainService blockedDomainService;

    @GetMapping
    public List<BlockedDomainDto> getAll() {
        return blockedDomainService.getAll();
    }

    @PostMapping
    public BlockedDomainDto create(@RequestBody CreateBlockedDomainRequest request) {
        return blockedDomainService.create(request);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importDomains(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID policyId) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            List<String> domains = reader.lines().collect(Collectors.toList());
            blockedDomainService.importDomains(domains, categoryId, policyId);
            return ResponseEntity.ok("Import réussi : " + domains.size() + " domaines traités");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        blockedDomainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}