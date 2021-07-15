package com.dnsabr.vad.mysite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoRestController {
    @GetMapping(value = "/welcome")
    public ResponseEntity welcomeEndpoint() {
        return ResponseEntity.ok("Welcome to Baeldung Spring Boot Demo!");
    }
}