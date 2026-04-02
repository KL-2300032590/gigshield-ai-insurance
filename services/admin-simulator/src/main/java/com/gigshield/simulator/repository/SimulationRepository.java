package com.gigshield.simulator.repository;

import com.gigshield.simulator.model.Simulation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * Repository for Simulation entities
 * 
 * Provides reactive MongoDB access for simulation history
 */
@Repository
public interface SimulationRepository extends ReactiveMongoRepository<Simulation, String> {

    /**
     * Find simulations by city
     *
     * @param city City name
     * @return Flux of simulations for the city
     */
    Flux<Simulation> findByCity(String city);

    /**
     * Find simulations by status
     *
     * @param status Simulation status
     * @return Flux of simulations with the given status
     */
    Flux<Simulation> findByStatus(String status);

    /**
     * Find simulations created after a timestamp
     *
     * @param timestamp Start timestamp
     * @return Flux of recent simulations
     */
    Flux<Simulation> findByCreatedAtAfter(Instant timestamp);

    /**
     * Find simulations by city and event type
     *
     * @param city City name
     * @param eventType Event type
     * @return Flux of matching simulations
     */
    Flux<Simulation> findByCityAndEventType(String city, String eventType);
}
