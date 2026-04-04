package com.parametrix.claim.repository;

import com.parametrix.common.model.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends MongoRepository<Claim, String> {
    
    List<Claim> findByWorkerIdOrderByCreatedAtDesc(String workerId);
    
    List<Claim> findByPolicyId(String policyId);
    
    boolean existsByPolicyIdAndTriggerType(String policyId, Claim.TriggerType triggerType);
    
    List<Claim> findByStatus(Claim.ClaimStatus status);
}
