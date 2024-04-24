package com.example.consumermicroservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.consumermicroservice.entity.ProcessedMessages;

import java.util.Optional;

@Repository
public interface ProcessedMessagesRepository extends JpaRepository<ProcessedMessages, Long> {
    Optional<ProcessedMessages> findByMessageId(String messageId);
}
