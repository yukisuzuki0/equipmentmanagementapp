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
    
    private String categoryLabel;  // 追加

    private String itemCode;
    
    private String itemLabel;      // 追加

    private int lifespanYears;
}