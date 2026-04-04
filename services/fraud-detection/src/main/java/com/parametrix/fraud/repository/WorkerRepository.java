package com.parametrix.fraud.repository;

import com.parametrix.common.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends MongoRepository<Worker, String> {
}
