package com.example.equipmentmanagement.dto;

import java.time.LocalDate;
import lombok.Data;

/**
 * 設備データ転送オブジェクト（DTO）クラス
 * 
 * このクラスは設備エンティティの情報を画面表示用に変換するためのDTOです。
 * エンティティの基本情報に加え、以下の計算済みフィールドを含みます：
 * - 減価償却関連の計算値
 * - 表示用のラベル情報
 * - 使用年数等の派生データ
 * 
 * 主な用途：
 * - 設備一覧画面での表示
 * - 設備詳細画面での表示
 * - JSON API レスポンス
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
@Data
public class EquipmentDto {
    /** 設備ID */
    private Integer id;
    /** 管理番号 */
    private String managementNumber;
    /** メインカテゴリーコード */
    private String categoryCode;
    /** サブカテゴリーコード */
    private String itemCode;
    /** 品名 */
    private String name;
    /** 購入価格 */
    private Double cost;
    /** 購入日 */
    private LocalDate purchaseDate;
    /** 数量 */
    private Integer quantity;
    /** 設置場所コード */
    private String locationCode;

    /** 耐用年数 */
    private Integer lifespanYears;
    /** 経過年数 */
    private Integer elapsedYears;
    /** 年間減価償却額 */
    private Double annualDepreciation;
    /** 帳簿価額 */
    private Double bookValue;

    // 追加項目
    /** 型番 */
    private String modelNumber;
    /** メーカー名 */
    private String manufacturer;
    /** 仕様 */
    private String specification;
    /** 減価償却状況（例：「終了」） */
    private String depreciationStatus;
    /** 使用不能フラグ */
    private Boolean isBroken;
    /** 貸出可能フラグ */
    private Boolean isAvailableForLoan;
    /** 設置場所表示用ラベル */
    private String locationLabel;
    /** 使用期限 */
    private LocalDate usageDeadline;
    /** カテゴリー名（表示用） */
    private String categoryName;
    /** サブカテゴリー名（表示用） */
    private String subcategoryName;
}

