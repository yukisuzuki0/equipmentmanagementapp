package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 設備リポジトリインターフェース
 * 
 * 設備エンティティに対するデータアクセス操作を提供します。
 * Spring Data JPAのJpaRepositoryを継承することで、
 * 基本的なCRUD操作が自動的に実装されます。
 * 
 * 利用可能な操作：
 * - findAll() : 全設備の取得
 * - findById(id) : IDによる設備の取得
 * - save(equipment) : 設備の保存（新規登録・更新）
 * - deleteById(id) : IDによる設備の削除
 * - delete(equipment) : 設備エンティティによる削除
 * - count() : 設備数のカウント
 * - existsById(id) : IDによる設備存在確認
 * 
 * @author Equipment Management Team
 * @version 1.0
 * @since 2024
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    // Spring Data JPAにより基本的なCRUD操作が自動実装される
    // 必要に応じてカスタムクエリメソッドを追加可能
}
