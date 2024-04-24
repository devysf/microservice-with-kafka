package com.example.producermicroservice.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.example.producermicroservice.producer.KafkaRelayService;

@RequiredArgsConstructor
@Component
public class ApplicationReadyListener {

    private final KafkaRelayService kafkaRelayService;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        new Thread(() -> {
            kafkaRelayService.run();
        }).start();
    }
}
