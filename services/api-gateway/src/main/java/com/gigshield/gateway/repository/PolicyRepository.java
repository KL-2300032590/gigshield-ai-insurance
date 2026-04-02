package com.gigshield.gateway.repository;

import com.gigshield.common.model.Policy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    
    Optional<Policy> findByWorkerIdAndWeekNumberAndYear(String workerId, int weekNumber, int year);
    
    List<Policy> findByWorkerIdOrderByCreatedAtDesc(String workerId);
    
    List<Policy> findByWorkerIdAndStatus(String workerId, Policy.PolicyStatus status);
    
    boolean existsByWorkerIdAndWeekNumberAndYear(String workerId, int weekNumber, int year);
}
