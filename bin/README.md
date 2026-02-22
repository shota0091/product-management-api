# product-management-api
# Spring Boot REST API Template

Spring Bootを用いた、実践的なRESTful APIのテンプレート実装です。
商品情報のCRUD（作成・読み取り・更新・削除）操作に加え、実務で頻出するBOM付きCSVダウンロード機能を実装しています。

## 🛠 テクノロジースタック
* **Java**: 17 (または使用したバージョンに合わせて変更)
* **Framework**: Spring Boot 3.x
* **Database**: H2 Database (インメモリDB)
* **ORM**: Spring Data JPA
* **Other**: Lombok

## ✨ 主な機能
* **CRUD API**: 商品データに対する基本的なREST操作
* **一括検索**: N+1問題を考慮した `findAllById` によるパフォーマンスチューニング
* **CSV出力機能**: Excelでの文字化けを防ぐBOM付きUTF-8でのCSVダウンロード

## 🚀 API エンドポイント一覧

| メソッド | エンドポイント | 説明 |
| :--- | :--- | :--- |
| `GET` | `/api/hello/getProductsInfo` | 全商品の一覧を取得 |
| `GET` | `/api/hello/getProductInfo/{id}` | 指定したIDの商品を取得 |
| `POST` | `/api/hello/saveProductInfo` | 新規商品の登録 |
| `POST` | `/api/hello/editProductInfo` | 既存商品の更新 |
| `DELETE`| `/api/hello/deleteProductInfo/{id}` | 指定したIDの商品を削除 |
| `GET` | `/api/hello/exportProductsCsv` | 全商品をCSV形式でダウンロード |
| `POST` | `/api/hello/outPutCSV/ProductInfo` | 指定した商品リストをCSV形式でダウンロード |

## ⚙️ ローカルでの起動方法
1. リポジトリをクローンします。
2. お使いのIDE（Eclipse, IntelliJなど）でMavenプロジェクトとしてインポートします。
3. `DemoApplication.java` を実行し、Spring Bootアプリケーションを起動します。
4. デフォルトでは `http://localhost:8080` でサーバーが立ち上がります。

## 📊 データベースの確認 (H2 Console)
アプリケーション起動中、以下のURLからインメモリデータベースの中身をGUIで確認できます。
* **URL**: `http://localhost:8080/h2-console`
* **JDBC URL**: `jdbc:h2:mem:testdb`