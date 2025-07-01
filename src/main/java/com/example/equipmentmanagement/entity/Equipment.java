package com.example.equipmentmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "management_number", nullable = false, unique = true)
    private String managementNumber;

    private String categoryCode;
    private String itemCode;
    private String name;
    private String modelNumber;           // 型番
    private String manufacturer;          // メーカー
    private String specification;         // 仕様
    private Double cost;
    private LocalDate purchaseDate;
    private Integer quantity = 1;
    private String locationCode;
    private Boolean isDisposed = false;

    private Integer lifespanYears;        // 耐用年数
    private Boolean isBroken = false;     // 使用不能
    private Boolean isAvailableForLoan = false; // 貸出可能
    private LocalDate usageDeadline;      // 使用期限

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
