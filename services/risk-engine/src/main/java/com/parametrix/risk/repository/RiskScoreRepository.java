package com.parametrix.risk.repository;

import com.parametrix.common.model.RiskScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RiskScoreRepository extends MongoRepository<RiskScore, String> {
    
    Optional<RiskScore> findByWorkerIdAndExpiresAtAfter(String workerId, Instant now);
    
    Optional<RiskScore> findTopByWorkerIdOrderByCalculatedAtDesc(String workerId);
}
