package com.fiap.hackaton.grp14.producer.kafka;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicInitializer {

    private final KafkaProducerConfig kafkaProducerConfig;

    public KafkaTopicInitializer(KafkaProducerConfig kafkaProducerConfig) {
        this.kafkaProducerConfig = kafkaProducerConfig;
    }

    @PostConstruct
    public void init() {
        kafkaProducerConfig.configureTopic();
    }
}

