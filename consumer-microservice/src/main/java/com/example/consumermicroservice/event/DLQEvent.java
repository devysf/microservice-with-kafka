package com.example.consumermicroservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DLQEvent {
    private String valueOfEvent;
    private String description;

}
