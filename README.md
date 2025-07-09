# 設備管理システム (Equipment Management System)

企業・組織の設備・備品を管理し、減価償却計算を自動化するWebアプリケーションです。

## 🚀 主な機能

- **設備管理**: 設備の登録・編集・削除・一覧表示
- **減価償却計算**: 定額法による自動計算と表示
- **カテゴリー管理**: メインカテゴリーとサブカテゴリーの階層構造
- **設置場所管理**: 設備の設置場所追跡
- **一括操作**: 複数設備の一括削除
- **管理番号**: 年度別の自動管理番号生成
- **検索機能**: 設置場所・品名による検索
- **Ajax対応**: カテゴリー選択時のサブカテゴリー動的更新

## 🛠 技術スタック

- **Backend**: Spring Boot 3.4.5
- **Frontend**: Thymeleaf テンプレート
- **Database**: MySQL 8.0 / H2 Database
- **Build Tool**: Maven
- **Java Version**: 21
- **Dependencies**: Spring Data JPA, Lombok, Validation

## 📋 システム要件

- Java 21以上
- MySQL 8.0以上（またはH2 Database）
- Maven 3.6以上
- Webブラウザ（Chrome、Firefox、Safari、Edge等）

## 🚀 クイックスタート

### 1. プロジェクトのクローン
```bash
git clone <repository-url>
cd equipment-management
```

### 2. データベース設定
`src/main/resources/application.properties` を編集：

```properties
# MySQL使用時
spring.datasource.url=jdbc:mysql://localhost:3306/tax_assets?useSSL=false&serverTimezone=Asia/Tokyo
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# H2 Database使用時（開発・テスト）
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

### 3. アプリケーション起動
```bash
# Windows
mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

### 4. アクセス
ブラウザで `http://localhost:8080/equipment/list` にアクセス

## 📁 プロジェクト構成

```
src/
├── main/
│   ├── java/com/example/equipmentmanagement/
│   │   ├── EquipmentManagementApplication.java    # メインアプリケーション
│   │   ├── controller/
│   │   │   └── EquipmentController.java          # Webコントローラー
│   │   ├── entity/
│   │   │   ├── Equipment.java                    # 設備エンティティ
│   │   │   ├── Category.java                     # カテゴリーエンティティ
│   │   │   ├── Subcategory.java                  # サブカテゴリーエンティティ
│   │   │   ├── UsefulLife.java                   # 耐用年数エンティティ
│   │   │   └── Location.java                     # 設置場所エンティティ
│   │   ├── repository/
│   │   │   ├── EquipmentRepository.java          # 設備リポジトリ
│   │   │   ├── CategoryRepository.java           # カテゴリーリポジトリ
│   │   │   ├── SubcategoryRepository.java        # サブカテゴリーリポジトリ
│   │   │   └── LocationRepository.java           # 設置場所リポジトリ
│   │   ├── service/
│   │   │   ├── EquipmentService.java             # 設備管理サービス
│   │   │   └── DepreciationService.java          # 減価償却計算サービス
│   │   └── dto/
│   │       ├── EquipmentDto.java                 # 設備DTO
│   │       └── CategoryOption.java               # カテゴリーオプションDTO
│   ├── resources/
│   │   ├── application.properties                # アプリケーション設定
│   │   └── templates/                           # Thymeleafテンプレート
│   │       ├── equipment_list.html              # 設備一覧画面
│   │       ├── equipment_create.html            # 設備登録画面
│   │       ├── equipment_edit.html              # 設備編集画面
│   │       ├── equipment_delete.html            # 複数削除画面
│   │       └── equipment_search.html            # 設備検索画面
└── test/                                        # テストファイル
```

## 📊 データベーステーブル

### equipment（設備テーブル）
| カラム名 | 型 | 説明 |
|---------|---|------|
| id | INT (PK) | 設備ID |
| management_number | VARCHAR (UNIQUE) | 管理番号 |
| category_code | VARCHAR | カテゴリーコード |
| item_code | VARCHAR | 品目コード |
| subcategory_id | INT | サブカテゴリーID |
| name | VARCHAR | 設備名 |
| model_number | VARCHAR | 型番 |
| manufacturer | VARCHAR | メーカー |
| specification | TEXT | 仕様 |
| cost | DECIMAL | 購入価格 |
| purchase_date | DATE | 購入日 |
| quantity | INT | 数量 |
| location_code | VARCHAR | 設置場所コード |
| lifespan_years | INT | 耐用年数 |
| is_broken | BOOLEAN | 故障フラグ |
| is_available_for_loan | BOOLEAN | 貸出可能フラグ |
| usage_deadline | DATE | 使用期限 |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### category（カテゴリーテーブル）
| カラム名 | 型 | 説明 |
|---------|---|------|
| id | INT (PK) | カテゴリーID |
| name | VARCHAR | カテゴリー名 |
| code | VARCHAR | カテゴリーコード |

### subcategory（サブカテゴリーテーブル）
| カラム名 | 型 | 説明 |
|---------|---|------|
| id | INT (PK) | サブカテゴリーID |
| name | VARCHAR | サブカテゴリー名 |
| category_id | INT (FK) | 親カテゴリーID |

### useful_life（耐用年数テーブル）
| カラム名 | 型 | 説明 |
|---------|---|------|
| id | INT (PK) | ID |
| subcategory_id | INT (FK) | サブカテゴリーID |
| useful_years | INT | 法定耐用年数 |

### location（設置場所テーブル）
| カラム名 | 型 | 説明 |
|---------|---|------|
| id | INT (PK) | 場所ID |
| code | VARCHAR | 場所コード |
| name | VARCHAR | 場所名 |
| parent_id | INT | 親場所ID |

## 🔧 API エンドポイント

| Method | URL | 説明 |
|--------|-----|------|
| GET | `/equipment/list` | 設備一覧表示 |
| GET | `/equipment/search` | 設備検索フォーム表示 |
| GET | `/equipment/search/results` | 設備検索結果表示 |
| GET | `/equipment/create-form` | 設備登録フォーム表示 |
| POST | `/equipment/create` | 設備新規登録 |
| GET | `/equipment/edit?id={id}` | 設備編集フォーム表示 |
| POST | `/equipment/update` | 設備更新 |
| POST | `/equipment/delete` | 設備削除 |
| GET | `/equipment/delete-mode` | 複数削除モード表示 |
| POST | `/equipment/delete-multiple` | 複数設備削除 |
| POST | `/equipment/update-location` | 設置場所更新 |
| GET | `/equipment/api/subcategories/{categoryId}` | サブカテゴリー一覧取得（Ajax） |

## 📈 減価償却計算

### 計算方式: 定額法
```
年間減価償却額 = 取得価額 ÷ 耐用年数
帳簿価額 = 取得価額 - 累積減価償却額
```

### 計算例
- 取得価額: 100,000円
- 耐用年数: 4年
- 年間減価償却額: 25,000円
- 3年経過後の帳簿価額: 25,000円

## 🎨 画面機能

### 設備一覧画面
- 全設備の一覧表示
- 減価償却計算結果の表示
- 設備状態による行の色分け
- 設置場所の即時更新
- 新規登録・一括削除への遷移

### 設備検索画面
- 設置場所による検索
- 品名（部分一致）による検索
- 複合条件検索（設置場所と品名）

### 設備登録画面
- 新規設備の登録
- カテゴリー選択時のサブカテゴリー自動更新
- 管理番号の自動生成
- バリデーション機能

### 設備編集画面
- 既存設備の情報更新
- 登録画面と同様の機能

### 複数削除画面
- チェックボックスによる複数選択
- 一括削除機能

## 🔒 セキュリティ

- 社内ネットワークでの使用を想定
- 外部公開時は認証機能の追加を推奨
- データベースの適切なパスワード設定
- 定期的なデータバックアップを推奨

## 🐛 トラブルシューティング

### アプリケーション起動失敗
- Javaバージョンの確認（Java 21以上）
- ポート8080の使用状況確認
- データベース接続設定の確認

### データベース接続エラー
- MySQLサーバーの起動確認
- 接続情報（URL、ユーザー名、パスワード）の確認
- ファイアウォール設定の確認

## 📝 開発情報

- **開発チーム**: Equipment Management Team
- **バージョン**: 1.0
- **ライセンス**: 内部使用のみ
- **作成日**: 2024年

## 📚 その他のドキュメント

- [設備管理システム取扱説明書.txt](./設備管理システム取扱説明書.txt) - 詳細な操作マニュアル
- ソースコード内のJavadocコメント - 技術的な詳細

## 🤝 貢献・サポート

システムに関するお問い合わせや改善提案は、開発チームまでご連絡ください。