package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.EquipmentLifespan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface EquipmentLifespanRepository extends JpaRepository<EquipmentLifespan, Integer> {
    Optional<EquipmentLifespan> findByCategoryCodeAndItemCode(String categoryCode, String itemCode);

    List<EquipmentLifespan> findByCategoryCode(String categoryCode);  // 追加
}

