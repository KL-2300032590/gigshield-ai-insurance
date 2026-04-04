package com.parametrix.claim.repository;

import com.parametrix.common.model.Policy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    
    List<Policy> findByWorkerIdOrderByCreatedAtDesc(String workerId);
    
    Optional<Policy> findByWorkerIdAndWeekNumberAndYear(String workerId, int weekNumber, int year);
    
    @Query("{ 'city': ?0, 'status': ?2, 'startDate': { $lte: ?1 }, 'endDate': { $gte: ?1 } }")
    List<Policy> findActivePoliciesInCity(String city, LocalDate date, Policy.PolicyStatus status);
    
    @Query("{ 'status': ?1, 'startDate': { $lte: ?0 }, 'endDate': { $gte: ?0 } }")
    List<Policy> findActivePolicies(LocalDate date, Policy.PolicyStatus status);
}
