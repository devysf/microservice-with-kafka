package com.example.producermicroservice.service;

import com.example.producermicroservice.repository.ProducerDomainRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.producermicroservice.entity.ProducerDomainEntity;
import com.example.producermicroservice.entity.Outbox;
import com.example.producermicroservice.event.InvalidDomainEvent;
import com.example.producermicroservice.event.ProducerDomainEvent;
import com.example.producermicroservice.repository.OutboxRepository;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProducerDomainService {
    private static final String DOMAIN_TOPIC = "DomainTopic";
    private final ProducerDomainRepository producerDomainRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void saveProducerDomainEntity(String name) throws JsonProcessingException {
        // 1. Make domain operation
        ProducerDomainEntity producerDomainEntity = new ProducerDomainEntity();
        producerDomainEntity.setName(name);
        producerDomainRepository.save(producerDomainEntity);

        // 2. create event
        ProducerDomainEvent producerDomainEvent =
                new ProducerDomainEvent(producerDomainEntity.getName());

        // 3. Save event to the outbox table
        Outbox outbox = createOutboxRecord(producerDomainEvent);
        outboxRepository.save(outbox);
    }

    private Outbox createOutboxRecord(ProducerDomainEvent producerDomainEvent) throws JsonProcessingException {
        String message = new ObjectMapper().writeValueAsString(producerDomainEvent);
        String messageId = String.valueOf(UUID.randomUUID());

        Outbox outbox = new Outbox();
        outbox.setMessage(message);
        outbox.setMessageId(messageId);
        outbox.setTopicName(DOMAIN_TOPIC);
        outbox.setPartitionNo(0);
        outbox.setMessageTime(LocalDateTime.now());
        return outbox;
    }

    @Transactional
    public void saveInvalidDomainEntity(String name) throws JsonProcessingException {
        ProducerDomainEntity producerDomainEntity = new ProducerDomainEntity();
        producerDomainEntity.setName(name);
        producerDomainRepository.save(producerDomainEntity);

        InvalidDomainEvent invalidDomainEvent =
                new InvalidDomainEvent(producerDomainEntity.getName(), new Random().nextInt());

        Outbox outbox = createOutboxRecord(invalidDomainEvent);
        outboxRepository.save(outbox);
    }
}
