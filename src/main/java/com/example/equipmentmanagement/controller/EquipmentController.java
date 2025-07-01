package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.EquipmentDto;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.repository.EquipmentRepository;
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

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private DepreciationService depreciationService;

    // カテゴリごとの品目コードマップ（例、必要に応じて変更）
    private static final Map<String, List<String>> categoryItemMap = Map.ofEntries(
    Map.entry("furniture", List.of(
        "desk_metal", "desk_other", "sofa_commercial", "sofa_other", "bed",
        "child_desk", "display_cool", "display_other", "furniture_commercial_other", "furniture_metal",
        "furniture_other", "audio", "aircon_heater", "fridge_washer", "freezer_non_elec",
        "fabric_goods", "rug_commercial", "rug_other", "interior_metal", "interior_other",
        "kitchen_glass", "kitchen_other", "other_metal", "other_other"
    )),
    Map.entry("office_communication", List.of(
        "mimeograph_typing_print", "mimeograph_typing_other", "personal_computer", "computer_other",
        "copier_cash_register", "office_equipment_other", "teletype_fax", "intercom_broadcast",
        "telephone_digital", "telephone_other"
    )),
    Map.entry("clock_measure", List.of(
        "clock", "weights", "test_measure_device"
    )),
    Map.entry("optical_photo", List.of(
        "camera_projection", "enlarger_dryer"
    )),
    Map.entry("signage_ad", List.of(
        "signage_neon_balloon", "mannequin_model", "other_metal", "other_other"
    )),
    Map.entry("container_safe", List.of(
        "cylinder_welded", "cylinder_forged_chlorine", "cylinder_forged_other", "drum_large_container",
        "drum_container_metal", "drum_container_other", "safe_handheld", "safe_other"
    )),
    Map.entry("beauty_equipment", List.of(
        "beauty_device"
    )),
    Map.entry("medical_equipment", List.of(
        "sterilization_device", "surgical_device", "dialysis_device", "rehab_device", "dispensing_device",
        "dental_unit", "optical_fiberscope", "optical_other", "xray_electronic_mobile", "xray_electronic_other",
        "other_ceramic_glass", "other_metal", "other_other"
    )),
    Map.entry("entertainment_sport", List.of(
        "ball_game_equipment", "pachinko_bingo_shooting", "board_games", "sports_equipment"
    ))
);


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
                int elapsed = Math.min(java.time.Period.between(e.getPurchaseDate(), LocalDate.now()).getYears(), lifespan);
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
        model.addAttribute("categoryOptions", categoryItemMap.keySet().stream().toList());
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

    // 編集画面表示（修正済み）
    @GetMapping("/edit")
    public String editEquipment(@RequestParam("id") Integer id, Model model) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        model.addAttribute("equipmentForm", equipment);
        model.addAttribute("locationOptions", getLocationOptions());
        model.addAttribute("categoryOptions", categoryItemMap.keySet().stream().toList());
        model.addAttribute("categoryItemMap", categoryItemMap);
        return "equipment_edit";
    }

    // 更新処理
    @PostMapping("/update")
    public String updateEquipment(@ModelAttribute Equipment equipment) {
        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
    }

    // 削除処理
    @PostMapping("/delete")
    public String deleteEquipment(@RequestParam("id") Integer id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment/list";
    }

    // 設置場所コード→ラベル
    private String convertLocationCodeToLabel(String code) {
        if (code == null) return "不明";
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

    // 設置場所変更用POSTメソッド（プルダウンから即更新）
    @PostMapping("/update-location")
    public String updateLocation(@RequestParam("id") Integer id, @RequestParam("locationCode") String locationCode) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        equipment.setLocationCode(locationCode);
        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
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
            } catch (NumberFormatException ignored) {}
        }

        return prefix + String.format("%04d", nextNumber);
    }
}
