package com.gigshield.simulator.repository;

import com.gigshield.common.model.Worker;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends ReactiveMongoRepository<Worker, String> {
}
