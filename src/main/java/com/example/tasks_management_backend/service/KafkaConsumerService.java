// package com.example.tasks_management_backend.service;

// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// @Service
// public class KafkaConsumerService {

// @KafkaListener(topics = "task-created", groupId = "task-group")
// public void listen(String message) {
// System.out.println("Received message: " + message);
// }
// }