package com.example.consumermicroservice.consumer;

import com.example.consumermicroservice.entity.Outbox;
import com.example.consumermicroservice.entity.ProcessedMessages;
import com.example.consumermicroservice.repository.OutboxRepository;
import com.example.consumermicroservice.repository.ProcessedMessagesRepository;
import com.example.consumermicroservice.service.ConsumerDomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.consumermicroservice.event.ConsumerDomainEvent;
import com.example.consumermicroservice.event.DLQEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaReceiverService {
    private static final String DOMAIN_TOPIC = "DomainTopic";
    private static final String DLQ_TOPIC = "DLQTopic";

    private static final String MESSAGE_ID = "id";
    private final KafkaConsumer kafkaConsumer;
    private final ConsumerDomainService consumerDomainService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final OutboxRepository outboxRepository;

    public void run() {
        // 1. Subscribe to the topic
        kafkaConsumer.subscribe(Arrays.asList(DOMAIN_TOPIC));

        ConsumerRecord<String, String> currentRecord;
        while (true) {
            // 2. Poll events one by one
            currentRecord = poll();
            try {
                // 3. check if the message is already consumed
                String messageId = new String(currentRecord.headers().headers(MESSAGE_ID).iterator().next().value());
                if (checkMessageIsAlreadyConsumed(currentRecord, messageId)) {
                    log.info("already consumed with messageId = {}", messageId);
                } else {
                    // 4. Start consume process
                    startConsumeProcess(currentRecord);
                    log.info("consume successfully with messageId = {} offset = {} value= {} ", messageId, currentRecord.offset(), currentRecord.value());
                }
            } catch (JsonProcessingException e) {
                log.error("Message is not processed. Message sent to DLQ");
                // send unprocessed message to dlq
                sendUnprocessedMessageToDLQ(currentRecord, e);
            } catch (Exception e) {
                log.error("unhandled exception occcur. Retry consume process");
                kafkaConsumer.seek(new TopicPartition(currentRecord.topic(), currentRecord.partition()), currentRecord.offset());
                continue;
            }

            commitOffsetToKafka(currentRecord);
        }
    }

    private boolean checkMessageIsAlreadyConsumed(ConsumerRecord<String, String> record, String messageId) {
        return processedMessagesRepository.findByMessageId(messageId).isPresent();
    }

    private ConsumerRecord<String, String> poll() {
        ConsumerRecords<String, String> records;
        while (true) {
            try {
                records = kafkaConsumer.poll(Duration.ofMillis(1000));
                if (!records.isEmpty()) {
                    // Since we set MAX_POLL_RECORDS_CONFIG to 1, we know that there will be 1 element in each poll.
                    return records.iterator().next();
                }
            } catch (Exception e) {
                log.error("poll failed", e);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startConsumeProcess(ConsumerRecord<String, String> record) throws JsonProcessingException {
        try {
            // 5. Make domain operation
            ConsumerDomainEvent consumerDomainEvent = new ObjectMapper().readValue(record.value(), ConsumerDomainEvent.class);
            consumerDomainService.consumeMessage(consumerDomainEvent);

            // 6. Save processed message to the database
            ProcessedMessages processedMessages = createProcessedMessage(record);
            processedMessagesRepository.save(processedMessages);
        } catch (Exception e) {
            log.error("exception in startConsumeProcess:", e.getMessage(), e);
            throw e;
        }

    }

    private ProcessedMessages createProcessedMessage(ConsumerRecord<String, String> record) {
        String messageId = new String(record.headers().headers(MESSAGE_ID).iterator().next().value());

        ProcessedMessages processedMessages = new ProcessedMessages();
        processedMessages.setMessageId(messageId);
        processedMessages.setTopicName(record.topic());
        processedMessages.setPartitionNo(record.partition());
        return processedMessages;
    }

    private void commitOffsetToKafka(ConsumerRecord<String, String> record) {
        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1, null);
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = Collections.singletonMap(topicPartition, offsetAndMetadata);
        kafkaConsumer.commitSync(offsetsToCommit);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendUnprocessedMessageToDLQ(ConsumerRecord<String, String> currentRecord, Exception e) {
        DLQEvent dlqEvent = new DLQEvent(currentRecord.value(), e.getMessage());
        Outbox outbox = createOutboxRecord(dlqEvent);
        outboxRepository.save(outbox);
    }

    @SneakyThrows
    private Outbox createOutboxRecord(DLQEvent dlqEvent) {
        String message = new ObjectMapper().writeValueAsString(dlqEvent);
        String messageId = String.valueOf(UUID.randomUUID());

        Outbox outbox = new Outbox();
        outbox.setMessage(message);
        outbox.setMessageId(messageId);
        outbox.setTopicName(DLQ_TOPIC);
        outbox.setPartitionNo(0);
        outbox.setMessageTime(LocalDateTime.now());
        return outbox;
    }
}
