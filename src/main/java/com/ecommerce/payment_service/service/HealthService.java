package com.ecommerce.payment_service.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthService {

    public Map<String, String> getStatus() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("service", "payment-service");
        status.put("version", "1.0.0");
        return status;
    }
}