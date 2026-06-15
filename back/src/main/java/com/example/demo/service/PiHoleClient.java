package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PiHoleClient {

    @Value("${pihole.api.url}")
    private String apiUrl;

    @Value("${pihole.api.password}")
    private String apiPassword;

    private final RestTemplate restTemplate;

private String sessionToken = null;
private String csrfToken = null;

private String getToken() {
    try {
        String url = apiUrl + "/api/auth";
        Map<String, String> body = Map.of("password", apiPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getBody() != null && response.getBody().containsKey("session")) {
            Map<String, Object> session = (Map<String, Object>) response.getBody().get("session");
            sessionToken = String.valueOf(session.get("sid"));
            csrfToken = String.valueOf(session.get("csrf"));
            return sessionToken;
        }
    } catch (Exception e) {
        System.err.println("Erreur auth Pi-hole : " + e.getMessage());
    }
    return null;
}

private HttpHeaders authHeaders() {
    if (sessionToken == null) getToken();
    HttpHeaders headers = new HttpHeaders();
    headers.set("sid", sessionToken);
    headers.set("X-CSRF-Token", csrfToken);
    return headers;
}

    // ── Récupérer les dernières requêtes DNS ──
    public List<Map<String, Object>> getRecentQueries() {
        try {
            String url = apiUrl + "/api/queries?max=100";
            HttpEntity<Void> request = new HttpEntity<>(authHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("queries")) {
                return (List<Map<String, Object>>) response.getBody().get("queries");
            }
        } catch (Exception e) {
            sessionToken = null;
            System.err.println("Erreur Pi-hole queries : " + e.getMessage());
        }
        return List.of();
    }

    // ── Statistiques Pi-hole ──
    public Map<String, Object> getStats() {
        try {
            String url = apiUrl + "/api/stats/summary";
            HttpEntity<Void> request = new HttpEntity<>(authHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (Exception e) {
            sessionToken = null;
            System.err.println("Erreur Pi-hole stats : " + e.getMessage());
            return Map.of();
        }
    }

public boolean blockDomain(String domain) {
    try {
        String url = apiUrl + "/api/domains/deny/exact";
        Map<String, Object> body = Map.of(
            "domain", domain,
            "comment", "Ajouté via Web Control"
        );
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, request, Map.class);
        return true;
    } catch (Exception e) {
        sessionToken = null;
        System.err.println("Erreur block domain : " + e.getMessage());
        return false;
    }
}

public boolean unblockDomain(String domain) {
    try {
        String url = apiUrl + "/api/domains/deny/exact/" + domain;
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        return true;
    } catch (Exception e) {
        sessionToken = null;
        System.err.println("Erreur unblock domain : " + e.getMessage());
        return false;
    }
}
}