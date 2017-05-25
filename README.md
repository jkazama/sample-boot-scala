sample-boot-scala
---

### はじめに

[Spring Boot](http://projects.spring.io/spring-boot/) / [Spring Security](http://projects.spring.io/spring-security/) / [Scala](http://www.scala-lang.org/) / [Skinny ORM](http://skinny-framework.org/documentation/orm.html) を元にしたサンプル実装です。  
ベーシックな基盤は [sample-boot-hibernate](https://github.com/jkazama/sample-boot-hibernate) を参考にしています。  
フレームワークではないのでプロジェクトを立ち上げる際に元テンプレートとして利用して下さい。

UI側の実装サンプルについては [sample-ui-vue](https://github.com/jkazama/sample-ui-vue) / [sample-ui-react](https://github.com/jkazama/sample-ui-react) を参照してください。

> 本サンプルはあくまで Spring Boot の利用を前提とした Scala 実装です。純粋に Scala での開発を行いたい方は素直に [SBT](http://www.scala-sbt.org/) ベースで [Play Framework](https://www.playframework.com/) / [Skinny Framework](http://skinny-framework.org/) といったフレームワークを利用して開発する事をオススメします。

---

本サンプルは Spring Boot を用いたドメインモデリングの実装例としても利用できます。

*※ JavaDoc に記載をしていますが参考実装レベルです。製品水準のコードが含まれているわけではありません。*

#### レイヤリングの考え方

オーソドックスな三層モデルですが、横断的な解釈としてインフラ層を考えています。

| レイヤ           | 特徴                                                        |
| -------------- | ----------------------------------------------------------- |
| UI             | ユースケース処理を公開 ( 必要に応じてリモーティングや外部サイトを連携 )          |
| アプリケーション     | ユースケース処理を集約 ( 外部リソースアクセスも含む )                       |
| ドメイン          | 純粋なドメイン処理 ( 外部リソースに依存しない )                           |
| インフラ          | DI コンテナや ORM 、各種ライブラリ、メッセージリソースの提供                    |

UI 層の公開処理は RESTfulAPI での API 提供のみをおこないます。 ( 利用クライアントは別途用意する必要があります )

#### Spring Boot の利用方針

Spring Boot は様々な利用方法が可能ですが、本サンプルでは以下のポリシーで利用します。

- 設定ファイルは yml を用いる。 Bean 定義に xml 等の拡張ファイルは用いない。
- 例外処理は終端 ( RestErrorAdvice / RestErrorCotroller ) で定義。 whitelabel 機能は無効化。
- ORM 実装として SkinnyORM を利用。
- Spring Security の認証方式はベーシック認証でなく、昔からよくある HttpSession で。
- 基礎的なユーティリティで Spring がサポートしていないのは簡易な実装を用意。

#### Scala コーディング方針

基盤インフラが Java という事もあり、基本は Better Java で。 （ あまり最適化をがんばらない ）

- 名称やパッケージは既存クラスと重複しても良いのでなるべく簡潔に。
- インターフェースの濫用をしない。
    - 基盤でも無いので DSL 的なものは作りこまずになるべくシンプルに。
- 列挙型は標準の Enum を利用せずに sealed trait で独自に定義。
- 副作用を避け case class を使えるところはなるべく使う。
    - var は Spring の DI 等、必要な部分のみに限定
- 例外処理は Either などを用いたプラグラマティックなアプローチを取らずに Java 風に上位へ投げ捨て。
    - 自己完結する例外処理をする際は Try を利用
    - 終端は AOP で一括処理する
- UI 層を SpringMVC と Scala とのブリッジ層として割り切って考える。
    - API の入力チェックは Java 側の仕組み ( JSR303 Bean Validation ) を利用する
    - アノテーションの実装も Java でオーソドックスに作る

#### パッケージ構成

パッケージ / リソース構成については以下を参照してください。

```
main
  java
    sample
      context                         … インフラ層
      controller                      … UI 層
      model                           … ドメイン層
      usecase                         … アプリケーション層
      util                            … 汎用ユーティリティ
      - Application.scala             … 実行可能な起動クラス
  resources
    - application.conf                … Skinny ORM 設定ファイル
    - application.yml                 … Spring Boot 設定ファイル
    - ehcache.xml                     … Spring Cache 設定ファイル
    - logback-spring.xml              … ロギング設定ファイル
    - messages-validation.properties  … 例外メッセージリソース
    - messages.properties             … メッセージリソース
```

### サンプルユースケース

サンプルユースケースとしては以下のようなシンプルな流れを想定します。

- **口座残高 100 万円を持つ顧客**が出金依頼(発生 T, 受渡 T + 3)をする。
- **システム**が営業日を進める。
- **システム**が出金依頼を確定する。 ( 確定させるまでは依頼取消行為を許容 )
- **システム**が受渡日を迎えた入出金キャッシュフローを口座残高へ反映する。

### 動作確認

本サンプルは [Gradle](https://gradle.org/) を利用しているので、 IDE やコンソールで手間なく動作確認を行うことができます。

*※ライブラリダウンロードなどが自動で行われるため、インターネット接続が可能な端末で実行してください。*

#### サーバ起動 （ IntelliJ IDEA ）

開発 IDE である [IntelliJ IDEA](https://www.jetbrains.com/idea/) で本サンプルを利用するには、事前に以下の手順を行っておく必要があります。

- JDK8 以上のインストール
- Scala 2.12 以上のインストール
- IntelliJ IDEA のインストール
- Scala プラグインのインストール

次の手順で本サンプルをプロジェクト化してください。  

1. *Import Project* でダウンロードした *sample-boot-scala* ディレクトリを選択
1. *Import project from external model* で *Gradle* を選択して *Next* を押下
1. 内容を確認して *Finish* を押下

*※ JDK の設定がされていない時は忘れずに設定してください*

次の手順で本サンプルを実行してください。

1. *Application.scala* に対し 「 右クリック -> Run Application 」
1. *Console* タブに 「 Started Application 」 という文字列が出力されればポート 8080 で起動が完了
1. ブラウザを立ち上げて 「 http://localhost:8080/api/management/health 」 で状態を確認

#### サーバ起動 （ Eclipse ）

開発 IDE である [Eclipse](https://eclipse.org/) で本サンプルを利用するには、事前に以下の手順を行っておく必要があります。

- JDK8 以上のインストール
- Scala 2.12 以上のインストール
- Eclipse ( Juno 以降 ) のインストール
- Gradle Plugin [ Buildship ] のインストール
    - Eclipse Mars 以降は Buildship 版が入っているので不要です
- Scala IDE のインストール
    - ダウンロードに失敗する時はローカルに updatesite のファイルをダウンロードして更新してください。
    - Scala IDE の選択条件に 2.12 を追加するには [BYOS](http://scala-ide.org/blog/scala-installations.html#BYOS) を参考にしてください。

次の手順で本サンプルをプロジェクト化してください。  

1. パッケージエクスプローラから 「 右クリック -> Import -> Project 」 で *Gradle Project* を選択して *Next* を押下
1. *Project root directory* にダウンロードした *sample-boot-scala* ディレクトリを指定して *Next* を押下
1. *Import Options* で *Next* を押下
1. *Gradle project structure* に *sample-boot-scala* が表示されたら *Finish* を押下 ( 依存ライブラリダウンロードがここで行われます )
1. 追加されたプロジェクト上で 「 右クリック -> Configure -> Add Scala Nature 」 を押下

次の手順で本サンプルを実行してください。

1. *Application.scala* に対し 「 右クリック -> Run As -> Scala Application 」
1. *Console* タブに 「 Started Application 」 という文字列が出力されればポート 8080 で起動が完了
1. ブラウザを立ち上げて 「 http://localhost:8080/api/management/health 」 で状態を確認

#### サーバ起動 （ コンソール ）

Windows / Mac のコンソールから実行するには Gradle のコンソールコマンドで行います。  

*※事前に JDK8 以上のインストールが必要です。*

1. ダウンロードした *sample-boot-scala* ディレクトリ直下へコンソールで移動
1. 「 gradlew bootRun 」 を実行
1. コンソールに 「 Started Application 」 という文字列が出力されればポート 8080 で起動が完了
1. ブラウザを立ち上げて 「 http://localhost:8080/api/management/health 」 で状態を確認

#### クライアント検証

Eclipse またはコンソールでサーバを立ち上げた後、 test パッケージ配下にある `sample.client.SampleClient` の各検証メソッドをユニットテストで実行してください。

##### 顧客向けユースケース

| URL                              | 処理                 | 実行引数 |
| -------------------------------- | ------------------- | ------------- |
| `/api/asset/cio/withdraw`        | 振込出金依頼          | [`accountId`: sample, `currency`: JPY, `absAmount`: 依頼金額] |
| `/api/asset/cio/unprocessedOut/` | 振込出金依頼未処理検索 | -       |

*※振込出金依頼はPOST、それ以外はGET*

##### 社内向けユースケース

| URL                     | 処理             | 実行引数                                           |
| ----------------------- | --------------- | ------------------------------------------------- |
| `/api/admin/asset/cio/` | 振込入出金依頼検索 | [`updFromDay`: yyyy-MM-dd, `updToDay`: yyyy-MM-dd]|

*※GET*

##### バッチ向けユースケース

| URL                                     | 処理                                          | 実行引数 |
| --------------------------------------- | --------------------------------------------- | ------ |
| `/api/system/job/daily/processDay`      | 営業日を進める(単純日回しのみ)                    | -      |
| `/api/system/job/daily/closingCashOut`  | 当営業日の出金依頼を締める                        | -      |
| `/api/system/job/daily/realizeCashflow` | 入出金キャッシュフローを実現する(受渡日に残高へ反映) | -      |

*※POST*

### 配布用 jar の作成

Spring Boot では Executable Jar ( ライブラリなども内包する実行可能 jar )を作成する事で単一の配布ファイルでアプリケーションを実行することができます。

1. コンソールから 「 gradlew build 」 を実行
1. `build/libs` 直下に jar が出力されるので Java8 以降の実行環境へ配布
1. 実行環境でコンソールから 「 java -jar xxx.jar 」 を実行して起動

*※実行引数に 「 --spring.profiles.active=[プロファイル名] 」 を追加する事で application.yml の設定値を変更できます。*  

### 依存ライブラリ

| ライブラリ               | バージョン | 用途/追加理由 |
| ----------------------- | -------- | ------------- |
| `spring-boot-starter-*` | 1.5.+    | Spring Boot 基盤 (actuator/security/aop/cache/web) |
| `scala-compiler/library`| 2.12.+   | Scala コンパイラ/基本ライブラリ |
| `skinny-orm`            | 2.3.+    | Scala ベースの ORM ライブラリ |
| `scalikejdbc-jsr310`    | 2.5.+    | Skinny ORM の JSR310 対応 |
| `ehcache`               | 3.2.+    | JCache 実装 |
| `commons-*`             | -        | 汎用ユーティリティライブラリ |
| `icu4j-*`               | 59.+     | 文字変換ライブラリ |

*※実際の詳細な定義は `build.gradle` を参照してください*

### 補足解説（インフラ層）

インフラ層の簡単な解説です。

*※細かい概要は実際にコードを読むか、 「 `gradlew scaladoc` 」 を実行して 「 `build/docs` 」 に出力されるドキュメントを参照してください*

#### DB / トランザクション

`sample.context.orm` 直下。
ドメイン実装をより Entity に寄せるための Skinny ORM サポート実装です。
トランザクション定義はトラブルの種となるのでアプリケーション層でのみ許し、なるべく狭く限定した形で付与しています。
Entity とテーブルのマッピングは autoConstruct の自動バインドを頼りますが、 Enum 等のカスタム要素を含む場合は extract でベタにマッピングしています。

#### 認証 / 認可

`sample.context.security` 直下。顧客 ( ROLE_USER ) / 社員 ( ROLE_ADMIN ) の 2 パターンを想定しています。それぞれのユーザ情報 ( UserDetails ) 提供手段は `sample.usecase.SecurityService` において定義しています。

認証 / 認可の機能を有効にするには `application.yml` の `extension.security.auth.enabled` に `true` を設定してください ( 標準ではテスト用途に false ) 。顧客 / 社員それぞれ同一VMでの相乗りは考えていません。社員専用モードで起動する時は起動時のプロファイル切り替え等で `extension.security.auth.admin` を `true` に設定してください。

#### 利用者監査

`sample.context.audit` 直下。「いつ」「誰が」「何をしたか」の情報を顧客 / システムそれぞれの視点で取得します。アプリケーション層での利用を想定しています。ログインした `Actor` の種別 ( User / System ) によって書き出し先と情報を切り替えています。運用時に行動証跡を取る際に利用可能です。

#### 例外

汎用概念としてフィールド単位にスタックした例外を持つ `ValidationException` を提供します。  
例外は末端のUI層でまとめて処理します。具体的にはアプリケーション層、ドメイン層では用途別の実行時例外をそのまま上位に投げるだけとし、例外捕捉は `sample.context.rest` 直下のコンポーネントにおいて AOP を用いた暗黙的差し込みを行います。
※ Either を I/F に用意するプログラマティックなアプローチはとらずに Java 風味で。

#### 日付 / 日時

`sample.context.Timestamper` を経由して Java8 で追加された `time` ライブラリを利用します。休日等を考慮した営業日算出はドメイン概念が含まれるので `sample.model.BusinessDayHandler` で別途定義しています。

#### キャッシング

`AccountService` 等で Spring が提供する @Cacheable を利用しています。 UI 層かアプリケーション層のどちらかに統一した方が良いですが、本サンプルではアプリケーション層だけ付与しています。

#### テスト

パターンとしては通常の Spring コンテナを用いる 2 パターン ( WebMock テスト / コンテナテスト ) と、 Skinny ORM だけに閉じた実行時間に優れたテスト ( Entity のみが対象 ) の合計 3 パターンで考えます。 （ それぞれ基底クラスは `ControllerSpecSupport` / `ContainerSpecSupport` / `UnitSpecSupport` ）  
テスト対象に Service まで含めるてしまうと冗長なので、そこら辺のカバレッジはあまり頑張らずに必要なものだけとしています。

サンプルでは controller を JUnit ベース、 model や util と ScalaTest ベースでテストしています。

### License

本サンプルのライセンスはコード含めて全て *MIT License* です。  
Spring Boot を用いたプロジェクト立ち上げ時のベース実装サンプルとして気軽にご利用ください。
