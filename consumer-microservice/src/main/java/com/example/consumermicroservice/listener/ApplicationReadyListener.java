package com.example.consumermicroservice.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.example.consumermicroservice.consumer.KafkaReceiverService;
import com.example.consumermicroservice.producer.KafkaRelayService;

@RequiredArgsConstructor
@Component
public class ApplicationReadyListener {

    private final KafkaReceiverService kafkaReceiver;
    private final KafkaRelayService kafkaRelayService;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        new Thread(() -> {
            kafkaReceiver.run();
        }).start();

        new Thread(() -> {
            kafkaRelayService.run();
        }).start();
    }


}
