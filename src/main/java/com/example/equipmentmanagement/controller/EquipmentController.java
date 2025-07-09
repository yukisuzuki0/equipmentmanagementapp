package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CategoryOption;
import com.example.equipmentmanagement.dto.EquipmentDto;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.*;
import com.example.equipmentmanagement.service.DepreciationService;
import com.example.equipmentmanagement.service.EquipmentService;

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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 設備管理コントローラークラス
 * 
 * 設備管理システムのWebコントローラーです。
 * 設備の一覧表示、登録、編集、削除等の画面処理とHTTPリクエストの処理を行います。
 * 
 * 主な機能：
 * - 設備検索
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
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final DepreciationService depreciationService;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Autowired
    public EquipmentController(
            EquipmentService equipmentService,
            DepreciationService depreciationService,
            LocationRepository locationRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository) {
        this.equipmentService = equipmentService;
        this.depreciationService = depreciationService;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    /**
     * ルートパスへのアクセスを検索画面にリダイレクト
     * 
     * @return 検索画面へのリダイレクト
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/equipment/list";
    }

    /**
     * 検索画面表示
     * 
     * 設備検索画面を表示します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 検索画面のテンプレート名
     */
    @GetMapping("/equipment/search")
    public String showSearchForm(Model model) {
        model.addAttribute("locationOptions", getLocationOptions());
        return "equipment_search";
    }

    /**
     * 検索実行処理
     * 
     * 検索条件に基づいて設備を検索し、結果を一覧表示します。
     * 
     * @param searchType 検索タイプ（location: 設置場所のみ、name: 品名のみ、both: 両方）
     * @param location 設置場所コード（オプショナル）
     * @param name 品名（部分一致、オプショナル）
     * @param model SpringのModelオブジェクト
     * @return 設備一覧画面のテンプレート名
     */
    @GetMapping("/equipment/search/results")
    public String searchEquipment(
            @RequestParam("searchType") String searchType,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "name", required = false) String name,
            Model model) {
        
        List<Equipment> equipments = equipmentService.searchEquipments(searchType, location, name);
        List<EquipmentDto> equipmentDtoList = equipmentService.convertToDtoList(equipments);
        
        // 設置場所名を取得
        String locationName = null;
        if (location != null && !location.isEmpty()) {
            try {
                Integer locationId = Integer.parseInt(location);
                locationName = locationRepository.findById(locationId)
                    .map(Location::getName)
                    .orElse("不明");
            } catch (NumberFormatException e) {
                locationName = "不明";
            }
        }
        
        model.addAttribute("equipments", equipmentDtoList);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchLocation", location);
        model.addAttribute("searchName", name);
        model.addAttribute("locationName", locationName);
        
        return "equipment_list";
    }

    /**
     * 設備一覧表示
     * 
     * 全設備の一覧を取得し、減価償却計算を行った結果を表示用DTOに変換して画面に渡します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 設備一覧画面のテンプレート名
     */
    @GetMapping("/equipment/list")
    public String getEquipmentList(Model model) {
        List<Equipment> equipments = equipmentService.getAllEquipments();
        List<EquipmentDto> equipmentDtoList = equipmentService.convertToDtoList(equipments);

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
    @GetMapping("/equipment/create-form")
    public String showCreateForm(Model model) {
        model.addAttribute("equipmentForm", new Equipment());
        model.addAttribute("locationOptions", getLocationOptions());
        model.addAttribute("categoryOptions", getCategoryOptionsFromDatabase());
        model.addAttribute("categoryItemMap", getCategoryItemMapFromDatabase());

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
    @PostMapping("/equipment/create")
    public String createEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        LocalDate purchaseDate = LocalDate.of(year, month, day);
        LocalDate usageDeadline = createUsageDeadline(usageYear, usageMonth, usageDay);
        
        equipmentService.createEquipment(equipment, purchaseDate, usageDeadline);
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
    @GetMapping("/equipment/edit")
    public String editEquipment(@RequestParam("id") Integer id, Model model) {
        Equipment equipment = equipmentService.getEquipmentById(id);
        model.addAttribute("equipmentForm", equipment);
        model.addAttribute("locationOptions", getLocationOptions());
        model.addAttribute("categoryOptions", getCategoryOptionsFromDatabase());
        model.addAttribute("categoryItemMap", getCategoryItemMapFromDatabase());

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
    @PostMapping("/equipment/update")
    public String updateEquipment(
            @RequestParam("purchaseYear") int year,
            @RequestParam("purchaseMonth") int month,
            @RequestParam("purchaseDay") int day,
            @RequestParam(value = "usageDeadlineYear", required = false) Integer usageYear,
            @RequestParam(value = "usageDeadlineMonth", required = false) Integer usageMonth,
            @RequestParam(value = "usageDeadlineDay", required = false) Integer usageDay,
            @ModelAttribute Equipment equipment) {

        LocalDate purchaseDate = LocalDate.of(year, month, day);
        LocalDate usageDeadline = createUsageDeadline(usageYear, usageMonth, usageDay);
        
        equipmentService.updateEquipment(equipment, purchaseDate, usageDeadline);
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
    @PostMapping("/equipment/delete")
    public String deleteEquipment(@RequestParam("id") Integer id) {
        equipmentService.deleteEquipmentById(id);
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
    @PostMapping("/equipment/update-location")
    public String updateLocation(@RequestParam("id") Integer id, @RequestParam("locationCode") String locationCode) {
        equipmentService.updateEquipmentLocation(id, locationCode);
        return "redirect:/equipment/list";
    }

    /**
     * カテゴリー選択時のサブカテゴリー取得API（Ajax用）
     * 
     * 指定されたカテゴリーIDに対応するサブカテゴリー一覧をJSON形式で返します。
     * 設備登録・編集画面でカテゴリー選択時に動的にサブカテゴリーを更新するために使用されます。
     * 
     * @param categoryId カテゴリーID
     * @return サブカテゴリー一覧（JSON形式）
     */
    @GetMapping("/equipment/api/subcategories/{categoryId}")
    @ResponseBody
    public List<Map<String, String>> getSubcategoriesByCategory(@PathVariable Integer categoryId) {
        return subcategoryRepository.findByCategoryId(categoryId)
                .stream()
                .map(subcategory -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", subcategory.getId().toString());
                    map.put("name", subcategory.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 削除モード画面表示
     * 
     * 複数の設備を選択して削除するための画面を表示します。
     * 
     * @param model SpringのModelオブジェクト
     * @return 削除モード画面のテンプレート名
     */
    @GetMapping("/equipment/delete-mode")
    public String showDeleteMode(Model model) {
        List<Equipment> equipments = equipmentService.getAllEquipments();
        List<EquipmentDto> equipmentDtoList = equipmentService.convertToDtoList(equipments);

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
    @PostMapping("/equipment/delete-multiple")
    public String deleteMultipleEquipment(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds) {
        equipmentService.deleteMultipleEquipments(selectedIds);
        return "redirect:/equipment/list";
    }

    // ==================== プライベートメソッド ====================

    /**
     * データベースからカテゴリ・サブカテゴリーのマッピングを取得
     * 
     * @return カテゴリーIDをキーとし、サブカテゴリーIDリストを値とするマップ
     */
    private Map<String, List<String>> getCategoryItemMapFromDatabase() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, List<String>> categoryItemMap = new HashMap<>();
        
        for (Category category : categories) {
            List<Subcategory> subcategories = subcategoryRepository.findByCategoryId(category.getId());
            List<String> subcategoryIds = subcategories.stream()
                    .map(subcategory -> subcategory.getId().toString())
                    .collect(Collectors.toList());
            categoryItemMap.put(category.getId().toString(), subcategoryIds);
        }
        
        return categoryItemMap;
    }

    /**
     * メインカテゴリーオプション（ID+名前）を取得
     * 
     * @return メインカテゴリーオプションのリスト
     */
    private List<CategoryOption> getCategoryOptionsFromDatabase() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> new CategoryOption(category.getId().toString(), category.getName()))
                .sorted(Comparator.comparing(CategoryOption::getCode))
                .toList();
    }

    /**
     * 設置場所オプション一覧を取得
     * 
     * @return 設置場所のIDと名前のマップ
     */
    private List<String> getLocationOptions() {
        List<Location> locations = locationRepository.findAll();
        
        if (locations.isEmpty()) {
            return new ArrayList<>();
        }
        
        return locations.stream()
            .map(location -> location.getId().toString())
            .collect(Collectors.toList());
    }

    /**
     * 使用期限のLocalDateオブジェクトを作成
     * 
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 使用期限のLocalDate（パラメータが不完全な場合はnull）
     */
    private LocalDate createUsageDeadline(Integer year, Integer month, Integer day) {
        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }
}