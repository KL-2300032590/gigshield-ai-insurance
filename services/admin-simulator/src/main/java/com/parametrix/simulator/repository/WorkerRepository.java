package com.parametrix.simulator.repository;

import com.parametrix.common.model.Worker;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends ReactiveMongoRepository<Worker, String> {
}
