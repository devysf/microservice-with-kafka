package com.example.consumermicroservice.service;

import com.example.consumermicroservice.entity.ConsumerDomainEntity;
import com.example.consumermicroservice.repository.ConsumerDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.consumermicroservice.event.ConsumerDomainEvent;

@Service
@RequiredArgsConstructor
public class ConsumerDomainService {

    private final ConsumerDomainRepository consumerDomainRepository;

    public void consumeMessage(ConsumerDomainEvent consumerDomainEvent) {
        ConsumerDomainEntity consumerDomainEntity = new ConsumerDomainEntity();
        consumerDomainEntity.setName(consumerDomainEvent.getName());
        consumerDomainRepository.save(consumerDomainEntity);
    }
}
