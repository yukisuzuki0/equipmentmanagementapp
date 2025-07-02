package com.example.equipmentmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 設備寿命エンティティクラス
 * 
 * 設備のカテゴリーと品目ごとの法定耐用年数を管理するエンティティです。
 * 減価償却計算の基準となる重要な情報を提供します。
 * 
 * 使用例：
 * - カテゴリーコード：PC、品目コード：DESKTOP → 4年
 * - カテゴリーコード：FURNITURE、品目コード：DESK → 8年
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
@Data
@Entity
@Table(name = "equipment_lifespan")
public class EquipmentLifespan {

    /** 寿命設定の一意識別子（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** カテゴリーコード（設備の大分類コード） */
    private String categoryCode;
    
    /** カテゴリーラベル（カテゴリーの表示名） */
    private String categoryLabel;

    /** 品目コード（設備の小分類コード） */
    private String itemCode;
    
    /** 品目ラベル（品目の表示名） */
    private String itemLabel;

    /** 法定耐用年数（年数） */
    private int lifespanYears;
}