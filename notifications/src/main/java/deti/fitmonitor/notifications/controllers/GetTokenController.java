package deti.fitmonitor.notifications.controllers;

import deti.fitmonitor.notifications.services.JwtUtilService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@CrossOrigin(origins = "http://localhost:4200") // Allow all origins for CORS
@RestController
@RequestMapping("/api/token")
public class GetTokenController {

    @Value("${external.auth.token.url}") // Define in application.properties
    private String tokenUrl;

    @Value("${external.auth.client.credentials}") // Define in application.properties
    private String clientCredentials;

    private final RestTemplate restTemplate;
    private final JwtUtilService jwtUtilService; // Add JwtUtilService

    // Caching map
    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();
    // Lock map to handle concurrent requests for the same code
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public GetTokenController(RestTemplate restTemplate, JwtUtilService jwtUtilService) {
        this.restTemplate = restTemplate;
        this.jwtUtilService = jwtUtilService; // Initialize JwtUtilService
    }

    // Handle GET request
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getToken(@RequestParam String code) throws Exception {

        // Check if the token is already cached
        if (tokenCache.containsKey(code)) {
           

            String jwtToken = tokenCache.get(code);

            JsonObject jsonObject = new JsonParser().parse(jwtToken).getAsJsonObject();

            String id_token = jsonObject.get("id_token").getAsString();


            // Extract roles from the token
            List<String> roles = jwtUtilService.extractRoles(id_token); // Use JwtUtilService


            return ResponseEntity.ok(getTokenResponse(jwtToken, roles));
        }

        // Create a lock for this code
        Lock lock = lockMap.computeIfAbsent(code, k -> new ReentrantLock());
        lock.lock(); // Acquire the lock

        try {
            // Double-check if the token is cached after acquiring the lock
            if (tokenCache.containsKey(code)) {

                String jwtToken = tokenCache.get(code);

                JsonObject jsonObject = new JsonParser().parse(jwtToken).getAsJsonObject();

                String id_token = jsonObject.get("id_token").getAsString();


                // Extract roles from the token
                List<String> roles = jwtUtilService.extractRoles(id_token); // Use JwtUtilService

                return ResponseEntity.ok(getTokenResponse(jwtToken, roles));
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Basic " + clientCredentials);

            // Prepare body
            String body = "grant_type=authorization_code" +
                          "&code=" + code +
                          "&redirect_uri=http://localhost:4200/callback";

            // Create request entity
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Make the request (POST to external API)
            ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            // Cache the response body (JWT token or error)
            String jwtToken = response.getBody();
            tokenCache.put(code, jwtToken);

            JsonObject jsonObject = new JsonParser().parse(jwtToken).getAsJsonObject();

            String id_token = jsonObject.get("id_token").getAsString();


            // Extract roles from the token
            List<String> roles = jwtUtilService.extractRoles(id_token); // Use JwtUtilService

            // Prepare the response
            return ResponseEntity.ok(getTokenResponse(jwtToken, roles));

        } finally {
            lock.unlock(); // Always release the lock
            lockMap.remove(code); // Remove the lock after usage
        }
    }

    // Helper method to create response map
    private Map<String, Object> getTokenResponse(String token) {
        return Map.of("token", token, "roles", List.of());
    }

    // Overloaded helper method to create response map with roles
    private Map<String, Object> getTokenResponse(String token, List<String> roles) {
        return Map.of("token", token, "roles", roles);
    }
}