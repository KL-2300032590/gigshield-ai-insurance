package com.parametrix.payout.repository;

import com.parametrix.common.model.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends MongoRepository<Claim, String> {
}
