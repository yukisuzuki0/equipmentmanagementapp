package com.example.equipmentmanagement.config;

import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * アプリケーション起動時にH2インメモリデータベースにサンプルデータを投入するクラス
 */
@Configuration
public class DataInitializer {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private EquipmentLifespanRepository equipmentLifespanRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // カテゴリー初期データ
            Category officeEquipment = new Category();
            officeEquipment.setName("オフィス機器");
            officeEquipment.setCode("OFC");
            categoryRepository.save(officeEquipment);
            
            Category itEquipment = new Category();
            itEquipment.setName("IT機器");
            itEquipment.setCode("ITC");
            categoryRepository.save(itEquipment);
            
            Category furniture = new Category();
            furniture.setName("家具");
            furniture.setCode("FRN");
            categoryRepository.save(furniture);
            
            // サブカテゴリー初期データ
            Subcategory computer = new Subcategory();
            computer.setName("コンピュータ");
            computer.setCategoryId(itEquipment.getId());
            subcategoryRepository.save(computer);
            
            Subcategory printer = new Subcategory();
            printer.setName("プリンタ");
            printer.setCategoryId(itEquipment.getId());
            subcategoryRepository.save(printer);
            
            Subcategory desk = new Subcategory();
            desk.setName("机");
            desk.setCategoryId(furniture.getId());
            subcategoryRepository.save(desk);
            
            Subcategory chair = new Subcategory();
            chair.setName("椅子");
            chair.setCategoryId(furniture.getId());
            subcategoryRepository.save(chair);
            
            // 設置場所初期データ
            Location mainOffice = new Location();
            mainOffice.setName("本社オフィス");
            mainOffice.setCode("MO");
            locationRepository.save(mainOffice);
            
            Location branch = new Location();
            branch.setName("支社");
            branch.setCode("BR");
            locationRepository.save(branch);
            
            // 耐用年数データ
            EquipmentLifespan computerLife = new EquipmentLifespan();
            computerLife.setCategoryCode(itEquipment.getCode());
            computerLife.setCategoryLabel(itEquipment.getName());
            computerLife.setItemCode("PC");
            computerLife.setItemLabel("パソコン");
            computerLife.setLifespanYears(4);
            equipmentLifespanRepository.save(computerLife);
            
            EquipmentLifespan furnitureLife = new EquipmentLifespan();
            furnitureLife.setCategoryCode(furniture.getCode());
            furnitureLife.setCategoryLabel(furniture.getName());
            furnitureLife.setItemCode("DK");
            furnitureLife.setItemLabel("デスク");
            furnitureLife.setLifespanYears(8);
            equipmentLifespanRepository.save(furnitureLife);
            
            // 設備サンプルデータ
            Equipment pc1 = new Equipment();
            pc1.setManagementNumber("ITC-PC-2024-001");
            pc1.setCategoryCode(itEquipment.getCode());
            pc1.setItemCode("PC");
            pc1.setSubcategoryId(computer.getId());
            pc1.setName("ノートPC");
            pc1.setModelNumber("Model X1");
            pc1.setManufacturer("Tech Corp");
            pc1.setSpecification("Core i7, 16GB RAM, 512GB SSD");
            pc1.setCost(150000.0);
            pc1.setPurchaseDate(LocalDate.of(2023, 4, 1));
            pc1.setQuantity(1);
            pc1.setLocationCode(mainOffice.getCode());
            pc1.setLifespanYears(4);
            pc1.setIsBroken(false);
            pc1.setIsAvailableForLoan(true);
            equipmentRepository.save(pc1);
            
            Equipment desk1 = new Equipment();
            desk1.setManagementNumber("FRN-DK-2023-001");
            desk1.setCategoryCode(furniture.getCode());
            desk1.setItemCode("DK");
            desk1.setSubcategoryId(desk.getId());
            desk1.setName("オフィスデスク");
            desk1.setModelNumber("Office Desk Pro");
            desk1.setManufacturer("Furniture Co.");
            desk1.setSpecification("木製、W1200 x D700 x H700mm");
            desk1.setCost(45000.0);
            desk1.setPurchaseDate(LocalDate.of(2022, 7, 15));
            desk1.setQuantity(5);
            desk1.setLocationCode(branch.getCode());
            desk1.setLifespanYears(8);
            desk1.setIsBroken(false);
            desk1.setIsAvailableForLoan(false);
            equipmentRepository.save(desk1);
        };
    }
} 