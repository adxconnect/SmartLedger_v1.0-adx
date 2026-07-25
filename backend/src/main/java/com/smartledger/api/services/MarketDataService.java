package com.smartledger.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class MarketDataService {

    @Value("${goldapi.key:YOUR_GOLDAPI_KEY_HERE}")
    private String goldApiKey;

    public String getMetalPrice(String metalSymbol) {
        // metalSymbol is expected to be XAU or XAG
        String url = "https://www.goldapi.io/api/" + metalSymbol + "/INR";
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", goldApiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch metal price. Ensure your API key is correct in application.properties.\"}";
        }
    }
}
