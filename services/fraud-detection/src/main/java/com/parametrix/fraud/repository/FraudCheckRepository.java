package com.parametrix.fraud.repository;

import com.parametrix.fraud.model.FraudCheck;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudCheckRepository extends MongoRepository<FraudCheck, String> {
    
    Optional<FraudCheck> findByClaimId(String claimId);
    
    List<FraudCheck> findByWorkerIdAndCheckedAtAfter(String workerId, Instant since);
    
    long countByWorkerIdAndCheckedAtAfter(String workerId, Instant since);
}
