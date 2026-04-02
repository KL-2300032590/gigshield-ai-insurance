package com.gigshield.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Admin Simulator Service - Weather hazard simulation for demonstrations
 * 
 * This service provides:
 * - REST API for triggering weather simulations
 * - Kafka integration for publishing synthetic events
 * - Simulation history tracking in MongoDB
 * - Integration with trigger-engine for claim workflow
 * 
 * Port: 8090
 * 
 * @author GigShield Team
 */
@SpringBootApplication
@EnableKafka
@EnableReactiveMongoRepositories
@ComponentScan(basePackages = {"com.gigshield.simulator", "com.gigshield.common"})
public class AdminSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminSimulatorApplication.class, args);
    }
}
