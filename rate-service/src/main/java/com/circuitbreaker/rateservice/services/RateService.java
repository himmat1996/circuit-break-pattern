package com.circuitbreaker.rateservice.services;

import com.circuitbreaker.rateservice.entities.Rate;
import com.circuitbreaker.rateservice.repositories.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateService {

    @Autowired
    private RateRepository repository;

    public Rate getRateByType(String type) {
        return repository.findByType(type).orElseThrow(() -> new RuntimeException("Rate Not Found: " + type));
    }
}
