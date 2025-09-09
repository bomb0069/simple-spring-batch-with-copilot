package com.example.batch.repository;

import com.example.batch.model.PriceCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceCalculationRepository extends JpaRepository<PriceCalculation, Long> {
}
