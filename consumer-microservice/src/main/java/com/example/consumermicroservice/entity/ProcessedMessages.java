package com.example.consumermicroservice.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "PROCESSED_MESSAGES")
public class ProcessedMessages  {
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="MESSAGE_ID", unique = true)
    private String messageId;

    @Column(name = "TOPIC_NAME")
    private String topicName;

    @Column(name = "PARTITION_NO")
    private int partitionNo;

}
