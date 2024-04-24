package com.example.consumermicroservice.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "OUTBOX")
public class Outbox {
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "IS_SENT")
    private boolean isSent;

    @Column(name = "TOPIC_NAME")
    private String topicName;

    @Column(name = "PARTITION_NO")
    private int partitionNo;

    @Column(name = "MESSAGE_ID", unique = true)
    private String messageId;

    @Column(name="MESSAGE")
    private String message;

    @Column(name="MESSAGE_TIME")
    private LocalDateTime messageTime;

}
