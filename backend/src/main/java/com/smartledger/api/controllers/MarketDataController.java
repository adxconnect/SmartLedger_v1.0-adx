package com.smartledger.api.controllers;

import com.smartledger.api.services.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/metal/{symbol}")
    public ResponseEntity<String> getMetalPrice(@PathVariable String symbol) {
        String result = marketDataService.getMetalPrice(symbol);
        if (result.contains("\"error\"")) {
            return ResponseEntity.status(500).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
