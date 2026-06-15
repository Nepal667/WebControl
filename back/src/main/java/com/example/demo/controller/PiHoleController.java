package com.example.demo.controller;

import com.example.demo.service.PiHoleClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pihole")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PiHoleController {

    private final PiHoleClient piHoleClient;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return piHoleClient.getStats();
    }

    @GetMapping("/queries")
    public List<Map<String, Object>> getRecentQueries() {
        return piHoleClient.getRecentQueries();
    }

    @PostMapping("/block")
    public ResponseEntity<String> blockDomain(@RequestParam String domain) {
        boolean ok = piHoleClient.blockDomain(domain);
        return ok
            ? ResponseEntity.ok("Domaine bloqué : " + domain)
            : ResponseEntity.internalServerError().body("Erreur lors du blocage");
    }

    @DeleteMapping("/block")
    public ResponseEntity<String> unblockDomain(@RequestParam String domain) {
        boolean ok = piHoleClient.unblockDomain(domain);
        return ok
            ? ResponseEntity.ok("Domaine débloqué : " + domain)
            : ResponseEntity.internalServerError().body("Erreur lors du déblocage");
    }
}