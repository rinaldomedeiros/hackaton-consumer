package com.fiap.hackaton.grp14.producer.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaVideoProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.kafka-topic}")
    private String topicName;

    public KafkaVideoProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVideoMessage(String videoId) {
        kafkaTemplate.send(topicName, videoId);
        System.out.println("Mensagem enviada ao Kafka para processar o vídeo: " + videoId);
    }
}

