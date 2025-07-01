package com.example.equipmentmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "location")
public class Location {

    @Id
    private String code;

    private String name;

    private String parentCode;
}
