package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.EquipmentLifespan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentLifespanRepository extends JpaRepository<EquipmentLifespan, Integer> {
    Optional<EquipmentLifespan> findByCategoryCodeAndItemCode(String categoryCode, String itemCode);
}
