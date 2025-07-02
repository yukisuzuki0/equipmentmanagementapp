package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.EquipmentLifespan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * 設備寿命リポジトリインターフェース
 * 
 * 設備寿命エンティティに対するデータアクセス操作を提供します。
 * Spring Data JPAのJpaRepositoryを継承し、カスタムクエリメソッドも定義しています。
 * 
 * 利用可能な操作：
 * - 基本的なCRUD操作（継承）
 * - カテゴリーコードと品目コードによる検索
 * - カテゴリーコードによる品目一覧取得
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
public interface EquipmentLifespanRepository extends JpaRepository<EquipmentLifespan, Integer> {
    
    /**
     * カテゴリーコードと品目コードで設備寿命を検索します
     * 
     * このメソッドは減価償却計算時に、特定の設備の耐用年数を
     * 取得するために使用されます。
     * 
     * @param categoryCode カテゴリーコード
     * @param itemCode 品目コード
     * @return 該当する設備寿命エンティティ（見つからない場合はEmpty）
     */
    Optional<EquipmentLifespan> findByCategoryCodeAndItemCode(String categoryCode, String itemCode);

    /**
     * カテゴリーコードで設備寿命一覧を検索します
     * 
     * このメソッドは設備登録・編集画面で、選択されたカテゴリーに
     * 対応する品目一覧を動的に表示するために使用されます。
     * 
     * @param categoryCode カテゴリーコード
     * @return 該当するカテゴリーの設備寿命エンティティ一覧
     */
    List<EquipmentLifespan> findByCategoryCode(String categoryCode);
}

