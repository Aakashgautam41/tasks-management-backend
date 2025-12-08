// package com.example.tasks_management_backend.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Service;

// @Service
// public class KafkaProducerService {
// private final KafkaTemplate<String, Object> kafkaTemplate;
// private static final Logger log =
// LoggerFactory.getLogger(KafkaProducerService.class);

// public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
// this.kafkaTemplate = kafkaTemplate;
// }

// public void sendTaskEvent(String topic, Object message) {
// kafkaTemplate.send(topic, message);
// log.info("Sent task event to Kafka topic: {}", topic);
// }

// }
