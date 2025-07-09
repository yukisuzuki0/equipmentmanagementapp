package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.dto.EquipmentDto;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * 設備管理サービスクラス
 * 
 * 設備管理に関するビジネスロジックを提供します。
 * コントローラーから分離された業務処理を担当します。
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final DepreciationService depreciationService;

    @Autowired
    public EquipmentService(
            EquipmentRepository equipmentRepository,
            LocationRepository locationRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository,
            DepreciationService depreciationService) {
        this.equipmentRepository = equipmentRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.depreciationService = depreciationService;
    }

    /**
     * 全設備を取得
     * 
     * @return 全設備のリスト
     */
    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    /**
     * 検索条件に基づいて設備を検索
     * 
     * @param searchType 検索タイプ
     * @param location 設置場所コード
     * @param name 品名
     * @return 検索結果の設備リスト
     */
    public List<Equipment> searchEquipments(String searchType, String location, String name) {
        switch (searchType) {
            case "location":
                if (location != null && !location.isEmpty()) {
                    return equipmentRepository.findByLocationCode(location);
                } else {
                    return equipmentRepository.findAll();
                }
            case "name":
                if (name != null && !name.isEmpty()) {
                    return equipmentRepository.findByNameContainingIgnoreCase(name);
                } else {
                    return List.of();
                }
            case "both":
                if (location != null && !location.isEmpty() && name != null && !name.isEmpty()) {
                    return equipmentRepository.findByLocationCodeAndNameContainingIgnoreCase(location, name);
                } else if (location != null && !location.isEmpty()) {
                    return equipmentRepository.findByLocationCode(location);
                } else if (name != null && !name.isEmpty()) {
                    return equipmentRepository.findByNameContainingIgnoreCase(name);
                } else {
                    return equipmentRepository.findAll();
                }
            default:
                return equipmentRepository.findAll();
        }
    }

    /**
     * IDで設備を取得
     * 
     * @param id 設備ID
     * @return 設備エンティティ
     * @throws RuntimeException 設備が見つからない場合
     */
    public Equipment getEquipmentById(Integer id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("設備が見つかりません: " + id));
    }

    /**
     * 設備を新規作成
     * 
     * @param equipment 設備エンティティ
     * @param purchaseDate 購入日
     * @param usageDeadline 使用期限
     */
    public void createEquipment(Equipment equipment, LocalDate purchaseDate, LocalDate usageDeadline) {
        equipment.setPurchaseDate(purchaseDate);
        equipment.setUsageDeadline(usageDeadline);
        
        // カテゴリーコードを取得して管理番号を自動生成
        String categoryCode = getCategoryCodeById(equipment.getCategoryCode());
        equipment.setManagementNumber(generateNextManagementNumber(categoryCode));
        
        equipmentRepository.save(equipment);
    }

    /**
     * 設備を更新
     * 
     * @param equipment 設備エンティティ
     * @param purchaseDate 購入日
     * @param usageDeadline 使用期限
     */
    public void updateEquipment(Equipment equipment, LocalDate purchaseDate, LocalDate usageDeadline) {
        equipment.setPurchaseDate(purchaseDate);
        equipment.setUsageDeadline(usageDeadline);
        equipmentRepository.save(equipment);
    }

    /**
     * 設備を削除
     * 
     * @param id 設備ID
     */
    public void deleteEquipmentById(Integer id) {
        equipmentRepository.deleteById(id);
    }

    /**
     * 複数の設備を削除
     * 
     * @param selectedIds 削除対象の設備IDリスト
     */
    public void deleteMultipleEquipments(List<Integer> selectedIds) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            equipmentRepository.deleteAllById(selectedIds);
        }
    }

    /**
     * 設備の設置場所を更新
     * 
     * @param id 設備ID
     * @param locationCode 新しい設置場所コード
     */
    public void updateEquipmentLocation(Integer id, String locationCode) {
        Equipment equipment = getEquipmentById(id);
        equipment.setLocationCode(locationCode);
        equipmentRepository.save(equipment);
    }

    /**
     * 設備エンティティリストをDTOリストに変換
     * 
     * @param equipments 設備エンティティリスト
     * @return 設備DTOリスト
     */
    public List<EquipmentDto> convertToDtoList(List<Equipment> equipments) {
        // 必要なデータを事前にバッチで取得
        
        // 全設置場所をIDをキーにしたマップとして取得
        Map<Integer, Location> locationMap = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, location -> location));
                
        // 全カテゴリーをIDをキーにしたマップとして取得
        Map<Integer, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, category -> category));
                
        // 全サブカテゴリーをIDをキーにしたマップとして取得
        Map<Integer, Subcategory> subcategoryMap = subcategoryRepository.findAll().stream()
                .collect(Collectors.toMap(Subcategory::getId, subcategory -> subcategory));
        
        // 一括変換
        return equipments.stream()
                .map(equipment -> convertToDto(equipment, locationMap, categoryMap, subcategoryMap))
                .collect(Collectors.toList());
    }

    /**
     * 設備エンティティをDTOに変換（最適化版）
     * 
     * @param equipment 設備エンティティ
     * @param locationMap 設置場所マップ
     * @param categoryMap カテゴリーマップ
     * @param subcategoryMap サブカテゴリーマップ
     * @return 設備DTO
     */
    private EquipmentDto convertToDto(
            Equipment equipment, 
            Map<Integer, Location> locationMap,
            Map<Integer, Category> categoryMap,
            Map<Integer, Subcategory> subcategoryMap) {
        EquipmentDto dto = new EquipmentDto();
        
        // 基本情報をコピー
        copyBasicInfo(dto, equipment);

        // 設置場所コードを表示用ラベルに変換（事前に取得したマップを使用）
        dto.setLocationLabel(convertLocationCodeToLabel(equipment.getLocationCode(), locationMap));
        
        // カテゴリー名とサブカテゴリー名を設定（事前に取得したマップを使用）
        setCategoryNames(dto, equipment, categoryMap, subcategoryMap);

        // 耐用年数を取得
        int lifespan = depreciationService.getLifespanYears(equipment);
        dto.setLifespanYears(lifespan);

        // 減価償却計算
        calculateDepreciation(dto, equipment, lifespan);

        return dto;
    }

    /**
     * 基本情報をDTOにコピーするヘルパーメソッド
     * 
     * @param dto コピー先のDTO
     * @param equipment コピー元のエンティティ
     */
    private void copyBasicInfo(EquipmentDto dto, Equipment equipment) {
        dto.setId(equipment.getId());
        dto.setManagementNumber(equipment.getManagementNumber());
        dto.setCategoryCode(equipment.getCategoryCode());
        dto.setItemCode(equipment.getItemCode());
        dto.setName(equipment.getName());
        dto.setModelNumber(equipment.getModelNumber());
        dto.setManufacturer(equipment.getManufacturer());
        dto.setSpecification(equipment.getSpecification());
        dto.setCost(equipment.getCost());
        dto.setPurchaseDate(equipment.getPurchaseDate());
        dto.setQuantity(equipment.getQuantity());
        dto.setLocationCode(equipment.getLocationCode());
        dto.setIsBroken(equipment.getIsBroken());
        dto.setIsAvailableForLoan(equipment.getIsAvailableForLoan());
        dto.setUsageDeadline(equipment.getUsageDeadline());
    }

    /**
     * カテゴリー名とサブカテゴリー名を設定（最適化版）
     * 
     * @param dto 設備DTO
     * @param equipment 設備エンティティ
     * @param categoryMap カテゴリーマップ
     * @param subcategoryMap サブカテゴリーマップ
     */
    private void setCategoryNames(
            EquipmentDto dto, 
            Equipment equipment, 
            Map<Integer, Category> categoryMap,
            Map<Integer, Subcategory> subcategoryMap) {
        try {
            if (equipment.getCategoryCode() != null) {
                Integer categoryId = Integer.parseInt(equipment.getCategoryCode());
                Category category = categoryMap.get(categoryId);
                if (category != null) {
                    dto.setCategoryName(category.getName());
                }
            }
            
            if (equipment.getItemCode() != null) {
                Integer subcategoryId = Integer.parseInt(equipment.getItemCode());
                Subcategory subcategory = subcategoryMap.get(subcategoryId);
                if (subcategory != null) {
                    dto.setSubcategoryName(subcategory.getName());
                }
            }
        } catch (NumberFormatException ex) {
            // IDの変換に失敗した場合は名前を設定しない
        }
    }

    /**
     * 減価償却計算を実行
     * 
     * @param dto 設備DTO
     * @param equipment 設備エンティティ
     * @param lifespan 耐用年数
     */
    private void calculateDepreciation(EquipmentDto dto, Equipment equipment, int lifespan) {
        if (equipment.getPurchaseDate() != null && lifespan > 0) {
            int elapsed = Math.min(java.time.Period.between(equipment.getPurchaseDate(), LocalDate.now()).getYears(), lifespan);
            dto.setElapsedYears(elapsed);

            if (elapsed > lifespan) {
                // 減価償却完了
                dto.setDepreciationStatus("終了");
                dto.setAnnualDepreciation(0.0);
                dto.setBookValue(0.0);
            } else {
                // 減価償却継続中
                double annualDep = depreciationService.calculateAnnualDepreciation(equipment);
                dto.setAnnualDepreciation(annualDep);
                double bookValue = depreciationService.calculateBookValue(equipment, LocalDate.now());
                dto.setBookValue(bookValue);
                dto.setDepreciationStatus(String.format("%.2f", annualDep));
            }
        } else {
            // 購入日や耐用年数が不明な場合
            dto.setElapsedYears(0);
            dto.setAnnualDepreciation(0.0);
            dto.setBookValue(dto.getCost());
            dto.setDepreciationStatus("-");
        }
    }

    /**
     * 設置場所コードを表示用ラベルに変換（最適化版）
     * 
     * @param code 設置場所コード
     * @param locationMap 設置場所マップ
     * @return 表示用ラベル
     */
    private String convertLocationCodeToLabel(String code, Map<Integer, Location> locationMap) {
        if (code == null) {
            return "不明";
        }
            
        try {
            Integer locationId = Integer.parseInt(code);
            Location location = locationMap.get(locationId);
            return location != null ? location.getName() : "不明";
        } catch (NumberFormatException e) {
            return "不明";
        }
    }

    // 元のメソッドは残しておき、内部実装を変更して互換性を保つ
    private String convertLocationCodeToLabel(String code) {
        Map<Integer, Location> locationMap = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, location -> location));
        return convertLocationCodeToLabel(code, locationMap);
    }

    // 元のメソッドを残しておき、内部実装を変更して互換性を保つ
    private void setCategoryNames(EquipmentDto dto, Equipment equipment) {
        Map<Integer, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, category -> category));
        Map<Integer, Subcategory> subcategoryMap = subcategoryRepository.findAll().stream()
                .collect(Collectors.toMap(Subcategory::getId, subcategory -> subcategory));
        setCategoryNames(dto, equipment, categoryMap, subcategoryMap);
    }

    /**
     * 設備エンティティをDTOに変換
     * 
     * @param equipment 設備エンティティ
     * @return 設備DTO
     */
    private EquipmentDto convertToDto(Equipment equipment) {
        Map<Integer, Location> locationMap = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, location -> location));
        Map<Integer, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, category -> category));
        Map<Integer, Subcategory> subcategoryMap = subcategoryRepository.findAll().stream()
                .collect(Collectors.toMap(Subcategory::getId, subcategory -> subcategory));
        return convertToDto(equipment, locationMap, categoryMap, subcategoryMap);
    }
    
    /**
     * カテゴリーIDからカテゴリーコードを取得
     * 
     * @param categoryId カテゴリーID
     * @return カテゴリーコード（見つからない場合は"EQ"を返す）
     */
    private String getCategoryCodeById(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return "EQ"; // デフォルト値
        }
        
        try {
            Integer id = Integer.parseInt(categoryId);
            return categoryRepository.findById(id)
                    .map(Category::getCode)
                    .orElse("EQ"); // 見つからない場合はデフォルト値
        } catch (NumberFormatException e) {
            return "EQ"; // 数値変換に失敗した場合はデフォルト値
        }
    }
    
    /**
     * 管理番号の自動生成
     * 
     * カテゴリーコードと現在年を使用して「カテゴリーコード年-連番」形式の管理番号を生成します。
     * 例：KG2024-0001, JT2024-0001, TM2024-0001...
     * 
     * @param categoryCode カテゴリーコード
     * @return 生成された管理番号
     */
    private String generateNextManagementNumber(String categoryCode) {
        int currentYear = Year.now().getValue();
        String managementNumberPrefix = categoryCode + currentYear + "-";

        // 既存の管理番号から最大番号を取得
        int nextSequentialNumber = findNextSequentialNumber(managementNumberPrefix);

        return managementNumberPrefix + String.format("%04d", nextSequentialNumber);
    }
    
    /**
     * 次の連番を見つける
     * 
     * 指定されたプレフィックスで始まる管理番号の最大連番に1を足した値を返します。
     * 
     * @param prefix 管理番号のプレフィックス
     * @return 次の連番
     */
    private int findNextSequentialNumber(String prefix) {
        List<String> existingNumbers = equipmentRepository.findAll().stream()
                .map(Equipment::getManagementNumber)
                .filter(num -> num != null && num.startsWith(prefix))
                .sorted()
                .toList();

        int nextNumber = 1;
        if (!existingNumbers.isEmpty()) {
            String lastNumber = existingNumbers.get(existingNumbers.size() - 1);
            try {
                nextNumber = Integer.parseInt(lastNumber.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 数値変換に失敗した場合はデフォルト値の1を使用
            }
        }
        
        return nextNumber;
    }
} 