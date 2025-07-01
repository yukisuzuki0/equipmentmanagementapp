package com.example.equipmentmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "equipment_lifespan")
public class EquipmentLifespan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String categoryCode;

    private String itemCode;

    private int lifespanYears;
}
