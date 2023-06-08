package com.circuitbreaker.loanservice.services;

import com.circuitbreaker.loanservice.dtos.InterestRate;
import feign.Headers;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "iam-service", url = "http://localhost:9000")
@Headers("Content-Type: application/json")
public interface FeignService {
    @GetMapping(value = "/api/rates/{type}")
    InterestRate getProduct(@PathVariable("type") String type);

    @Component
    static class Fallback implements FeignService {

        @Override
        public InterestRate getProduct(String type) {
            return null;
        }
    }
}
