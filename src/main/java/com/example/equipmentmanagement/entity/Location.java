package com.example.equipmentmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 設置場所エンティティクラス
 * 
 * 設備の設置場所を管理するエンティティです。
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
@Data
@Entity
@Table(name = "location")
public class Location {

    /** 場所ID（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 場所名（表示用、例：東京本店、仙台支店等） */
    private String name;
}
