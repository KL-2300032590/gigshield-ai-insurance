package com.gigshield.payout.repository;

import com.gigshield.common.model.Payout;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends MongoRepository<Payout, String> {
    
    List<Payout> findByWorkerIdOrderByCreatedAtDesc(String workerId);
    
    Optional<Payout> findByClaimId(String claimId);
    
    boolean existsByClaimId(String claimId);
    
    List<Payout> findByStatus(Payout.PayoutStatus status);
}
