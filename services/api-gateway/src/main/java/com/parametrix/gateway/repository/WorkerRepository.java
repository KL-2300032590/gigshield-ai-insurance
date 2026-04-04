package com.parametrix.gateway.repository;

import com.parametrix.common.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends MongoRepository<Worker, String> {
    
    Optional<Worker> findByEmail(String email);
    
    Optional<Worker> findByPhone(String phone);
    
    Optional<Worker> findByEmailOrPhone(String email, String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}
