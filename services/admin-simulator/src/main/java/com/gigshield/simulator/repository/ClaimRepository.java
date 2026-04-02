package com.gigshield.simulator.repository;

import com.gigshield.common.model.Claim;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends ReactiveMongoRepository<Claim, String> {
}
