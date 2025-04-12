package com.fiap.hackaton.grp14.producer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.kafka-topic:video-process-topic:video-process-topic}")
    private String topicName;

    @Value("${spring.kafka.topic.partitions:3}")
    private int partitions;

    @Value("${spring.kafka.topic.replication-factor:1}")
    private short replicationFactor;


    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public AdminClient adminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(configs);
    }

    public void configureTopic() {
        try (AdminClient adminClient = adminClient()) {
            if (doesTopicExist(adminClient)) {
                handleExistingTopic(adminClient);
            } else {
                createNewTopic(adminClient);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error configuring Kafka topic: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted state if interrupted
        }
    }

    private boolean doesTopicExist(AdminClient adminClient) throws ExecutionException, InterruptedException {
        return adminClient.listTopics(new ListTopicsOptions().listInternal(false))
                .names()
                .get()
                .contains(topicName);
    }

    private void handleExistingTopic(AdminClient adminClient) throws ExecutionException, InterruptedException {
        log.info("Topic '{}' already exists.", topicName);

        TopicDescription topicDescription = describeTopic(adminClient);
        if (topicDescription.partitions().size() == partitions) {
            log.info("Topic '{}' already has the requested {} partitions.", topicName, partitions);
            return;
        }

        increasePartitions(adminClient);
    }

    private TopicDescription describeTopic(AdminClient adminClient) throws ExecutionException, InterruptedException {
        DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
        return result.topicNameValues().get(topicName).get();
    }

    private void increasePartitions(AdminClient adminClient) throws ExecutionException, InterruptedException {
        Map<String, NewPartitions> newPartitions = Collections.singletonMap(
                topicName, NewPartitions.increaseTo(partitions)
        );
        adminClient.createPartitions(newPartitions).all().get();
        log.info("Increased the number of partitions for topic '{}' to {}.", topicName, partitions);
    }

    private void createNewTopic(AdminClient adminClient) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        log.info("Created new topic '{}' with {} partitions and replication factor {}.", topicName, partitions, replicationFactor);
    }


}

