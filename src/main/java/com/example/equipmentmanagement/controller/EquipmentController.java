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

/**
 * 設備管理コントローラークラス
 * 
 * 設備管理システムのWebコントローラーです。
 * 設備の一覧表示、登録、編集、削除等の画面処理とHTTPリクエストの処理を行います。
 * 
 * 主な機能：
 * - 設備一覧表示（減価償却計算込み）
 * - 設備の新規登録
 * - 設備の編集・更新
 * - 設備の削除（単体・複数）
 * - 設置場所の変更
 * - カテゴリー選択時の品目動的更新（Ajax）
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    /** 設備リポジトリ */
    @Autowired
    private EquipmentRepository equipmentRepository;

    /** 設備寿命リポジトリ */
    @Autowired
    private EquipmentLifespanRepository equipmentLifespanRepository;

    /** 減価償却計算サービス */
    @Autowired
    private DepreciationService depreciationService;

    /**
     * 設備一覧表示
     * 
     * 全設備の一覧を取得し、減価償却計算を行った結果を表示用DTOに変換して画面に渡します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 設備一覧画面のテンプレート名
     */
    @GetMapping("/list")
    public String getEquipmentList(Model model) {
        List<Equipment> equipments = equipmentRepository.findAll();

        // エンティティをDTO（表示用）に変換
        List<EquipmentDto> equipmentDtoList = equipments.stream().map(e -> {
            EquipmentDto dto = new EquipmentDto();
            // 基本情報をコピー
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

            // 設置場所コードを表示用ラベルに変換
            dto.setLocationLabel(convertLocationCodeToLabel(e.getLocationCode()));

            // 耐用年数を取得
            int lifespan = depreciationService.getLifespanYears(e);
            dto.setLifespanYears(lifespan);

            // 減価償却計算
            if (e.getPurchaseDate() != null && lifespan > 0) {
                int elapsed = Math.min(java.time.Period.between(e.getPurchaseDate(), LocalDate.now()).getYears(),
                        lifespan);
                dto.setElapsedYears(elapsed);

                if (elapsed > lifespan) {
                    // 減価償却完了
                    dto.setDepreciationStatus("終了");
                    dto.setAnnualDepreciation(0.0);
                    dto.setBookValue(0.0);
                } else {
                    // 減価償却継続中
                    double annualDep = depreciationService.calculateAnnualDepreciation(e);
                    dto.setAnnualDepreciation(annualDep);
                    double bookValue = depreciationService.calculateBookValue(e, LocalDate.now());
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

            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("equipments", equipmentDtoList);
        return "equipment_list";
    }

    /**
     * 設備登録フォーム表示
     * 
     * 新規設備登録画面を表示します。
     * カテゴリーオプションと設置場所オプションを取得してフォームに渡します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 設備登録画面のテンプレート名
     */
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

    /**
     * 設備登録処理
     * 
     * フォームから送信された設備データを保存します。
     * 管理番号を自動生成し、購入日と使用期限を設定します。
     * 
     * @param year 購入年
     * @param month 購入月
     * @param day 購入日
     * @param usageYear 使用期限年（オプショナル）
     * @param usageMonth 使用期限月（オプショナル）
     * @param usageDay 使用期限日（オプショナル）
     * @param equipment 設備エンティティ
     * @return 設備一覧画面へのリダイレクト
     */
    @PostMapping("/create")
    public String createEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        // 購入日を設定
        equipment.setPurchaseDate(LocalDate.of(year, month, day));

        // 使用期限を設定（任意）
        if (usageYear != null && usageMonth != null && usageDay != null) {
            equipment.setUsageDeadline(LocalDate.of(usageYear, usageMonth, usageDay));
        }

        // 管理番号を自動生成
        equipment.setManagementNumber(generateNextManagementNumber());
        equipmentRepository.save(equipment);

        return "redirect:/equipment/list";
    }

    /**
     * 設備編集画面表示
     * 
     * 指定されたIDの設備の編集画面を表示します。
     * 
     * @param id 編集対象の設備ID
     * @param model SpringのModelオブジェクト
     * @return 設備編集画面のテンプレート名
     */
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

    /**
     * 設備更新処理
     * 
     * 編集フォームから送信された設備データで既存の設備を更新します。
     * 
     * @param year 購入年
     * @param month 購入月
     * @param day 購入日
     * @param usageYear 使用期限年（オプショナル）
     * @param usageMonth 使用期限月（オプショナル）
     * @param usageDay 使用期限日（オプショナル）
     * @param equipment 設備エンティティ
     * @return 設備一覧画面へのリダイレクト
     */
    @PostMapping("/update")
    public String updateEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        // 購入日を設定
        equipment.setPurchaseDate(LocalDate.of(year, month, day));

        // 使用期限を設定（任意）
        if (usageYear != null && usageMonth != null && usageDay != null) {
            equipment.setUsageDeadline(LocalDate.of(usageYear, usageMonth, usageDay));
        } else {
            equipment.setUsageDeadline(null);
        }

        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
    }

    /**
     * 設備削除処理（単体）
     * 
     * 指定されたIDの設備を削除します。
     * 
     * @param id 削除対象の設備ID
     * @return 設備一覧画面へのリダイレクト
     */
    @PostMapping("/delete")
    public String deleteEquipment(@RequestParam("id") Integer id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment/list";
    }

    /**
     * 設置場所変更処理
     * 
     * プルダウンメニューからの即時更新で設備の設置場所を変更します。
     * 
     * @param id 対象設備のID
     * @param locationCode 新しい設置場所コード
     * @return 設備一覧画面へのリダイレクト
     */
    @PostMapping("/update-location")
    public String updateLocation(@RequestParam("id") Integer id, @RequestParam("locationCode") String locationCode) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        equipment.setLocationCode(locationCode);
        equipmentRepository.save(equipment);
        return "redirect:/equipment/list";
    }

    /**
     * カテゴリー選択時の品目取得API（Ajax用）
     * 
     * 指定されたカテゴリーコードに対応する品目一覧をJSON形式で返します。
     * 設備登録・編集画面でカテゴリー選択時に動的に品目を更新するために使用されます。
     * 
     * @param categoryCode カテゴリーコード
     * @return 品目一覧（JSON形式）
     */
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

    /**
     * データベースからカテゴリ・品目コードマッピングを取得
     * 
     * @return カテゴリーコードをキーとし、品目コードリストを値とするマップ
     */
    private Map<String, List<String>> getCategoryItemMapFromDatabase() {
        List<EquipmentLifespan> lifespans = equipmentLifespanRepository.findAll();

        return lifespans.stream()
                .collect(Collectors.groupingBy(
                        EquipmentLifespan::getCategoryCode,
                        Collectors.mapping(
                                EquipmentLifespan::getItemCode,
                                Collectors.toList())));
    }

    /**
     * カテゴリーオプション（コード+ラベル）を取得
     * 
     * @return カテゴリーオプションのリスト
     */
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

    /**
     * 設置場所コードを表示用ラベルに変換
     * 
     * @param code 設置場所コード
     * @return 表示用ラベル
     */
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

    /**
     * 設置場所オプション一覧を取得
     * 
     * @return 設置場所コードのリスト
     */
    private List<String> getLocationOptions() {
        return List.of("TOKYO", "SENDAI", "NIIGATA", "YOKOHAMA", "OSAKA", "SAITAMA");
    }

    /**
     * 管理番号の自動生成
     * 
     * 現在年を使用して「EQ年-連番」形式の管理番号を生成します。
     * 例：EQ2024-0001, EQ2024-0002...
     * 
     * @return 生成された管理番号
     */
    private String generateNextManagementNumber() {
        int year = Year.now().getValue();
        String prefix = "EQ" + year + "-";

        // 既存の管理番号から最大番号を取得
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

    /**
     * 削除モード画面表示
     * 
     * 複数の設備を選択して削除するための画面を表示します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 削除モード画面のテンプレート名
     */
    @GetMapping("/delete-mode")
    public String showDeleteMode(Model model) {
        List<Equipment> equipments = equipmentRepository.findAll();

        // 設備一覧表示と同様の処理でDTOに変換
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
        return "equipment_delete";
    }

    /**
     * 複数設備削除処理
     * 
     * チェックボックスで選択された複数の設備を一括削除します。
     * 
     * @param selectedIds 削除対象の設備IDリスト
     * @return 設備一覧画面へのリダイレクト
     */
    @PostMapping("/delete-multiple")
    public String deleteMultipleEquipment(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds) {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            equipmentRepository.deleteAllById(selectedIds);
        }
        return "redirect:/equipment/list";
    }
}