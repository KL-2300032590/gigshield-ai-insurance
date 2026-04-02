package com.gigshield.gateway.repository;

import com.gigshield.common.model.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends MongoRepository<Claim, String> {
    
    List<Claim> findByWorkerId(String workerId);
    
    List<Claim> findByPolicyId(String policyId);
    
    List<Claim> findByStatus(Claim.ClaimStatus status);
    
    List<Claim> findByWorkerIdAndStatus(String workerId, Claim.ClaimStatus status);
}
