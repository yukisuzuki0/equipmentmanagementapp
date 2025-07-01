package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
}
