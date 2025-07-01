package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CategoryOption;
import com.example.equipmentmanagement.dto.EquipmentDto;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.EquipmentLifespan;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.example.equipmentmanagement.repository.EquipmentLifespanRepository;
import com.example.equipmentmanagement.service.DepreciationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentLifespanRepository equipmentLifespanRepository;

    @Autowired
    private DepreciationService depreciationService;

    // 一覧表示
    @GetMapping("/list")
    public String getEquipmentList(Model model) {
        List<Equipment> equipments = equipmentRepository.findAll();

        List<EquipmentDto> equipmentDtoList = equipments.stream().map(e -> {
            EquipmentDto dto = new EquipmentDto();
            dto.setId(e.getId());
            dto.setManagementNumber(e.getManagementNumber());
            dto.setCategoryCode(e.getCategoryCode());
            dto.setItemCode(e.getItemCode());
            dto.setName(e.getName());
            dto.setModelNumber(e.getModelNumber());
            dto.setManufacturer(e.getManufacturer());
            dto.setSpecification(e.getSpecification());
            dto.setCost(e.getCost());
            dto.setPurchaseDate(e.getPurchaseDate());
            dto.setQuantity(e.getQuantity());
            dto.setLocationCode(e.getLocationCode());
            dto.setIsDisposed(e.getIsDisposed());
            dto.setIsBroken(e.getIsBroken());
            dto.setIsAvailableForLoan(e.getIsAvailableForLoan());
            dto.setUsageDeadline(e.getUsageDeadline());

            dto.setLocationLabel(convertLocationCodeToLabel(e.getLocationCode()));

            int lifespan = depreciationService.getLifespanYears(e);
            dto.setLifespanYears(lifespan);

            if (e.getPurchaseDate() != null && lifespan > 0) {
                int elapsed = Math.min(java.time.Period.between(e.getPurchaseDate(), LocalDate.now()).getYears(),
                        lifespan);
                dto.setElapsedYears(elapsed);

                if (elapsed > lifespan) {
                    dto.setDepreciationStatus("終了");
                    dto.setAnnualDepreciation(0.0);
                    dto.setBookValue(0.0);
                } else {
                    double annualDep = depreciationService.calculateAnnualDepreciation(e);
                    dto.setAnnualDepreciation(annualDep);
                    double bookValue = depreciationService.calculateBookValue(e, LocalDate.now());
                    dto.setBookValue(bookValue);
                    dto.setDepreciationStatus(String.format("%.2f", annualDep));
                }
            } else {
                dto.setElapsedYears(0);
                dto.setAnnualDepreciation(0.0);
                dto.setBookValue(dto.getCost());
                dto.setDepreciationStatus("-");
            }

            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("equipments", equipmentDtoList);
        return "equipment_list";
    }

    // 登録フォーム表示
    @GetMapping("/create-form")
    public String showCreateForm(Model model) {
        model.addAttribute("equipmentForm", new Equipment());
        model.addAttribute("locationOptions", getLocationOptions());

        // カテゴリーオプション（コード+ラベル）を取得
        List<CategoryOption> categoryOptions = getCategoryOptionsFromDatabase();
        model.addAttribute("categoryOptions", categoryOptions);

        // データベースからカテゴリとアイテムのマッピングを取得
        Map<String, List<String>> categoryItemMap = getCategoryItemMapFromDatabase();
        model.addAttribute("categoryItemMap", categoryItemMap);

        return "equipment_create";
    }

    // 登録処理
    @PostMapping("/create")
    public String createEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        equipment.setPurchaseDate(LocalDate.of(year, month, day));

        if (usageYear != null && usageMonth != null && usageDay != null) {
            equipment.setUsageDeadline(LocalDate.of(usageYear, usageMonth, usageDay));
        }

        equipment.setManagementNumber(generateNextManagementNumber());
        equipmentRepository.save(equipment);

        return "redirect:/equipment/list";
    }

    // 編集画面表示
    @GetMapping("/edit")
    public String editEquipment(@RequestParam("id") Integer id, Model model) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        model.addAttribute("equipmentForm", equipment);
        model.addAttribute("locationOptions", getLocationOptions());

        // カテゴリーオプション（コード+ラベル）を取得
        List<CategoryOption> categoryOptions = getCategoryOptionsFromDatabase();
        model.addAttribute("categoryOptions", categoryOptions);

        // データベースからカテゴリとアイテムのマッピングを取得
        Map<String, List<String>> categoryItemMap = getCategoryItemMapFromDatabase();
        model.addAttribute("categoryItemMap", categoryItemMap);

        return "equipment_edit";
    }

    // 更新処理
    // 更新処理
    @PostMapping("/update")
    public String updateEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        equipment.setPurchaseDate(LocalDate.of(year, month, day));

        if (usageYear != null && usageMonth != null && usageDay != null) {
            equipment.setUsageDeadline(LocalDate.of(usageYear, usageMonth, usageDay));
        } else {
            equipment.setUsageDeadline(null);
        }

        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
    }

    // 削除処理
    @PostMapping("/delete")
    public String deleteEquipment(@RequestParam("id") Integer id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment/list";
    }

    // 設置場所変更用POSTメソッド（プルダウンから即更新）
    @PostMapping("/update-location")
    public String updateLocation(@RequestParam("id") Integer id, @RequestParam("locationCode") String locationCode) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        equipment.setLocationCode(locationCode);
        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
    }

    // カテゴリ選択時のAjaxレスポンス
    @GetMapping("/api/items/{categoryCode}")
    @ResponseBody
    public List<Map<String, String>> getItemsByCategory(@PathVariable String categoryCode) {
        return equipmentLifespanRepository.findByCategoryCode(categoryCode)
                .stream()
                .map(lifespan -> Map.of(
                        "code", lifespan.getItemCode(),
                        "label", lifespan.getItemLabel() != null ? lifespan.getItemLabel() : lifespan.getItemCode()))
                .collect(Collectors.toList());
    }

    // データベースからカテゴリ・品目コードマッピングを取得
    private Map<String, List<String>> getCategoryItemMapFromDatabase() {
        List<EquipmentLifespan> lifespans = equipmentLifespanRepository.findAll();

        return lifespans.stream()
                .collect(Collectors.groupingBy(
                        EquipmentLifespan::getCategoryCode,
                        Collectors.mapping(
                                EquipmentLifespan::getItemCode,
                                Collectors.toList())));
    }

    // カテゴリーオプション（コード+ラベル）を取得
    private List<CategoryOption> getCategoryOptionsFromDatabase() {
        List<EquipmentLifespan> lifespans = equipmentLifespanRepository.findAll();

        Map<String, String> map = lifespans.stream()
                .collect(Collectors.toMap(
                        EquipmentLifespan::getCategoryCode,
                        EquipmentLifespan::getCategoryLabel,
                        (existing, replacement) -> existing // 重複は無視
                ));

        return map.entrySet().stream()
                .map(e -> new CategoryOption(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CategoryOption::getCode))
                .toList();
    }

    // 設置場所コード→ラベル
    private String convertLocationCodeToLabel(String code) {
        if (code == null)
            return "不明";
        return switch (code.toUpperCase()) {
            case "TOKYO" -> "東京本店";
            case "SENDAI" -> "仙台支店";
            case "NIIGATA" -> "新潟支店";
            case "YOKOHAMA" -> "横浜支店";
            case "OSAKA" -> "大阪支店";
            case "SAITAMA" -> "埼玉支店";
            default -> "不明";
        };
    }

    private List<String> getLocationOptions() {
        return List.of("TOKYO", "SENDAI", "NIIGATA", "YOKOHAMA", "OSAKA", "SAITAMA");
    }

    // 管理番号の自動生成
    private String generateNextManagementNumber() {
        int year = Year.now().getValue();
        String prefix = "EQ" + year + "-";

        List<String> numbers = equipmentRepository.findAll().stream()
                .map(Equipment::getManagementNumber)
                .filter(num -> num != null && num.startsWith(prefix))
                .sorted()
                .toList();

        int nextNumber = 1;
        if (!numbers.isEmpty()) {
            String last = numbers.get(numbers.size() - 1);
            try {
                nextNumber = Integer.parseInt(last.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", nextNumber);
    }
}