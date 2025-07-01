package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, String> {
}
