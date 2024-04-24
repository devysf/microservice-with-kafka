package com.example.consumermicroservice.producer;

import com.example.consumermicroservice.entity.Outbox;
import com.example.consumermicroservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaRelayService {

    private final KafkaProducer kafkaProducer;
    private final OutboxRepository outboxRepository;

    public void run() {

        while (true) {

            try {
                // 1. QUERY THE TABLE
                List<Outbox> outboxList = outboxRepository.findAllByIsSentFalse();
                if (outboxList.isEmpty()) {
                    Thread.sleep(1000);
                    continue;
                }

                // 2. SEND EVENTS TO THE KAFKA
                for (Outbox outbox : outboxList) {
                    ProducerRecord producerRecord = createProducerRecord(outbox);
                    send(producerRecord);
                    outbox.setSent(true);
                }

                // 3. UPDATE SENT RECORD
                outboxRepository.saveAll(outboxList);

            } catch (Exception e) {
                log.error("exception in KafkaRelayService ", e.getMessage(), e);
            }

        }
    }

    private ProducerRecord createProducerRecord(Outbox outbox) {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(outbox.getTopicName(), outbox.getPartitionNo(), null, outbox.getMessage());
        long timestamp = outbox.getMessageTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        producerRecord.headers().add(new RecordHeader("id", outbox.getMessageId().getBytes()));
        producerRecord.headers().add(new RecordHeader("timestamp", Long.valueOf(timestamp).toString().getBytes()));
        return producerRecord;
    }

    private void send(ProducerRecord producerRecord) {
        String id = new String(producerRecord.headers().headers("id").iterator().next().value());
        kafkaProducer.send(producerRecord, (metadata, exception) -> {
            if (exception == null) {
                log.info("message sent to kafka with {}-{}, message id: {}", producerRecord.topic(), producerRecord.partition(), id);
            } else {
                log.error("message cannot be sent to kafka with topic {} : message id: {}", producerRecord.topic(), id, exception);
            }
        });
    }

}
