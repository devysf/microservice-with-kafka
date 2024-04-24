package com.example.producermicroservice.controller;

import com.example.producermicroservice.service.ProducerDomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/producerDomain")
@RequiredArgsConstructor
public class ProducerDomainController {

    private final ProducerDomainService producerDomainService;

    @GetMapping("/save")
    public void save() throws JsonProcessingException {
        producerDomainService.saveProducerDomainEntity("test - " + UUID.randomUUID());
    }

    @GetMapping("/saveInvalidData")
    public void saveInvalidData() throws JsonProcessingException {
        producerDomainService.saveInvalidDomainEntity("test - " + UUID.randomUUID());
    }
}