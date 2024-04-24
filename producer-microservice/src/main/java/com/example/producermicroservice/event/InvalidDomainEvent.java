package com.example.producermicroservice.event;

import lombok.Data;

@Data
public class InvalidDomainEvent extends ProducerDomainEvent {
    private int age;

    public InvalidDomainEvent(String name, int age) {
        super(name);
        this.age = age;
    }
}
