package com.example.equipmentmanagement.controller;

/**import com.example.equipmentmanagement.dto.CategoryOption;*/
import com.example.equipmentmanagement.dto.EquipmentDto;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.*;
import com.example.equipmentmanagement.service.DepreciationService;
import com.example.equipmentmanagement.service.EquipmentService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**import java.util.Comparator;*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

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
    
    // 1ページあたりの表示件数
    private static final int PAGE_SIZE = 50;

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
     * @param location 設置場所（オプショナル、検索結果から戻る場合に使用）
     * @param name 品名（オプショナル、検索結果から戻る場合に使用）
     * @param model SpringのModelオブジェクト
     * @return 検索画面のテンプレート名
     */
    @GetMapping("/equipment/search")
    public String showSearchForm(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "name", required = false) String name,
            Model model) {
        model.addAttribute("locationOptions", getLocationOptions());
        model.addAttribute("location", location);
        model.addAttribute("name", name);
        return "equipment_search";
    }

    /**
     * 検索実行処理
     * 
     * 検索条件に基づいて設備を検索し、結果を一覧表示します。
     * 
     * @param location 設置場所コード（オプショナル）
     * @param name 品名（部分一致、オプショナル）
     * @param page ページ番号（0ベース）
     * @param model SpringのModelオブジェクト
     * @return 設備一覧画面のテンプレート名
     */
    @GetMapping("/equipment/search/results")
    public String searchEquipment(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        // ページネーション情報を作成
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        
        // 検索条件に基づいて設備を検索
        String searchType = determineSearchType(location, name);
        Page<Equipment> equipmentPage = equipmentService.searchEquipments(searchType, location, name, pageable);
        Page<EquipmentDto> equipmentDtoPage = equipmentService.convertToDtoPage(equipmentPage);
        
        // 設置場所名を取得
        String locationName = null;
        if (location != null && !location.isEmpty()) {
            locationName = getLocationNameById(location);
        }
        
        // 検索パラメータをモデルに追加（ページネーション用）
        model.addAttribute("currentUrl", "/equipment/search/results");
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchLocation", location);
        model.addAttribute("searchName", name);
        
        addSearchAttributesToModel(model, equipmentDtoPage, searchType, location, name, locationName);
        
        // ページネーション情報をモデルに追加
        addPaginationToModel(model, equipmentDtoPage);
        
        return "equipment_list";
    }
    
    /**
     * 検索結果をモデルに追加
     * 
     * @param model モデル
     * @param equipmentDtoPage 設備DTOページ
     * @param searchType 検索タイプ
     * @param location 設置場所コード
     * @param name 品名
     * @param locationName 設置場所名
     */
    private void addSearchAttributesToModel(
            Model model, 
            Page<EquipmentDto> equipmentDtoPage, 
            String searchType, 
            String location, 
            String name, 
            String locationName) {
        model.addAttribute("equipments", equipmentDtoPage.getContent());
        model.addAttribute("page", equipmentDtoPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchLocation", location);
        model.addAttribute("searchName", name);
        model.addAttribute("locationName", locationName);
        
        // ページ番号のリストを作成（1ベースのページ番号）
        int totalPages = Math.max(1, equipmentDtoPage.getTotalPages());
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
            .boxed()
            .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
    }
    
    /**
     * 検索条件から検索タイプを決定
     * 
     * @param location 設置場所
     * @param name 品名
     * @return 検索タイプ（location, name, both）
     */
    private String determineSearchType(String location, String name) {
        boolean hasLocation = location != null && !location.isEmpty();
        boolean hasName = name != null && !name.isEmpty();
        
        if (hasLocation && hasName) {
            return "both";
        } else if (hasLocation) {
            return "location";
        } else if (hasName) {
            return "name";
        } else {
            return "both"; // デフォルト値
        }
    }

    /**
     * 設置場所IDから設置場所名を取得
     * 
     * @param locationId 設置場所ID
     * @return 設置場所名（見つからない場合は「不明」）
     */
    private String getLocationNameById(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            return null;
        }
        
        try {
            Integer id = Integer.parseInt(locationId);
            return locationRepository.findById(id)
                .map(Location::getName)
                .orElse("不明");
        } catch (NumberFormatException e) {
            return "不明";
        }
    }

    /**
     * 設備一覧表示
     * 
     * 全設備の一覧を取得し、減価償却計算を行った結果を表示用DTOに変換して画面に渡します。
     * 
     * @param page ページ番号（0ベース）
     * @param model SpringのModelオブジェクト
     * @return 設備一覧画面のテンプレート名
     */
    @GetMapping("/equipment/list")
    public String getEquipmentList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        // ページネーション情報を作成
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        
        // ページネーション付きで全設備を取得
        Page<Equipment> equipmentPage = equipmentService.getAllEquipments(pageable);
        Page<EquipmentDto> equipmentDtoPage = equipmentService.convertToDtoPage(equipmentPage);

        model.addAttribute("equipments", equipmentDtoPage.getContent());
        model.addAttribute("page", equipmentDtoPage);
        model.addAttribute("currentUrl", "/equipment/list");
        
        // ページ番号のリストを作成（最大10ページまで表示）
        addPaginationToModel(model, equipmentDtoPage);
        
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
        addCommonFormAttributes(model);
        return "equipment_create";
    }

    /**
     * フォームに共通の属性を追加
     * 
     * @param model モデル
     */
    private void addCommonFormAttributes(Model model) {
        model.addAttribute("locationOptions", getLocationOptions());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("categoryItemMap", getCategoryItemMapFromDatabase());
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
        addCommonFormAttributes(model);
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
     * @param page ページ番号（0ベース）
     * @param model SpringのModelオブジェクト
     * @return 削除モード画面のテンプレート名
     */
    @GetMapping("/equipment/delete-mode")
    public String showDeleteMode(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        // ページネーション情報を作成
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        
        // ページネーション付きで全設備を取得
        Page<Equipment> equipmentPage = equipmentService.getAllEquipments(pageable);
        Page<EquipmentDto> equipmentDtoPage = equipmentService.convertToDtoPage(equipmentPage);

        model.addAttribute("equipments", equipmentDtoPage.getContent());
        model.addAttribute("page", equipmentDtoPage);
        model.addAttribute("currentUrl", "/equipment/delete-mode");
        
        // ページネーション情報をモデルに追加
        addPaginationToModel(model, equipmentDtoPage);
        
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
   
    private List<CategoryOption> getCategoryOptionsFromDatabase() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> new CategoryOption(category.getId().toString(), category.getName()))
                .sorted(Comparator.comparing(CategoryOption::getCode))
                .toList();
    }
    */
    /**
     * 設置場所オプション一覧を取得
     * 
     * @return 設置場所のIDリスト
     */
    private List<String> getLocationOptions() {
        // データベースから設置場所を取得
        List<Location> locations = locationRepository.findAll();
        
        if (locations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 重複を排除するためにStreamのdistinctを使用
        return locations.stream()
            .map(location -> location.getId().toString())
            .distinct() // 重複を排除
            .collect(Collectors.toList());
    }

    /**
     * 使用期限のLocalDateオブジェクトを作成
     * 
     * 年月日の各パラメータからLocalDateオブジェクトを作成します。
     * いずれかのパラメータがnullの場合はnullを返します。
     * 
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 使用期限のLocalDate（パラメータが不完全な場合はnull）
     */
    private LocalDate createUsageDeadline(Integer year, Integer month, Integer day) {
        boolean hasAllDateComponents = year != null && month != null && day != null;
        
        if (hasAllDateComponents) {
            try {
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                // 無効な日付の場合はnullを返す
                return null;
            }
        }
        
        return null;
    }

    /**
     * ページネーション情報をモデルに追加
     * 
     * @param model モデル
     * @param page ページ情報
     */
    private void addPaginationToModel(Model model, Page<?> page) {
        int currentPage = page.getNumber() + 1; // 1ベースのページ番号（表示用）
        int totalPages = Math.max(1, page.getTotalPages());
        
        List<Integer> pageNumbers = new ArrayList<>();
        
        // 表示するページボタンの最大数
        int maxDisplayedPages = 20;
        
        if (totalPages <= maxDisplayedPages) {
            // 総ページ数が表示可能最大数以下の場合、すべてのページ番号を表示
            for (int i = 1; i <= totalPages; i++) {
                pageNumbers.add(i);
            }
        } else {
            // 現在のページを中心にして表示するページ範囲を決定
            int half = maxDisplayedPages / 2;
            
            // 開始ページと終了ページを計算
            int startPage = Math.max(1, currentPage - half);
            int endPage = startPage + maxDisplayedPages - 1;
            
            // 終了ページが総ページ数を超える場合、調整
            if (endPage > totalPages) {
                endPage = totalPages;
                startPage = Math.max(1, endPage - maxDisplayedPages + 1);
            }
            
            // 開始ページが1より大きい場合、最初のページと省略記号を追加
            if (startPage > 1) {
                pageNumbers.add(1);
                if (startPage > 2) {
                    pageNumbers.add(-1); // -1 は省略記号を表す特別な値
                }
            }
            
            // 範囲内のページ番号を追加
            for (int i = startPage; i <= endPage; i++) {
                pageNumbers.add(i);
            }
            
            // 終了ページが最終ページより小さい場合、省略記号と最後のページを追加
            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    pageNumbers.add(-1); // -1 は省略記号を表す特別な値
                }
                pageNumbers.add(totalPages);
            }
        }
        
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPageNumber", currentPage); // 現在のページ番号（1ベース）
    }
}