package com.suyos.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suyos.user.model.ProcessedEvent;

/**
 * Repository for processed event entity data access operations.
 * 
 * <p>Provides standard CRUD operations for processed event entities and
 * specific query methods.</p>
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String>  {
    
}