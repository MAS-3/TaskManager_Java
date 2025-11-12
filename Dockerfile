# --- ステージ 1: ビルド環境 (Mavenで .jar を作る) ---

# Java 21 (JDK) と Maven が入ったイメージをベースにする
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# 作業ディレクトリを作成
WORKDIR /app

# (1) まず pom.xml (設計図) だけコピーして、ライブラリを先にダウンロードする
# (ソースコードより先にやることで、ビルドキャッシュが効きやすくなる)
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline

# (2) 次にソースコードをコピーする
COPY src src

# (3) アプリケーションをビルド（.jar ファイルを作成）する
# (テストはスキップして高速化)
RUN mvn package -DskipTests

# --- ステージ 2: 実行環境 (できた .jar を動かす) ---

# Java 21 の実行環境 (JRE) だけの小さなイメージをベースにする
FROM eclipse-temurin:21-jre-jammy

# (4) データベースファイル(/data/taskdb)を保存するためのディレクトリを作成
RUN mkdir /data

# (5) ビルドステージ(builder)から、完成した .jar ファイルをコピー
# (target/*.jar という書き方で、バージョン名が違っても自動で見つけてくれる)
COPY --from=builder /app/target/*.jar /app.jar

# (6) コンテナが 8080 ポートを使うことを宣言
EXPOSE 8080

# (7) コンテナ起動時に、アプリを実行する
# ★ここでH2 DBをファイルベース（永続化）にする設定を渡している
ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:h2:file:/data/taskdb", "-Dspring.jpa.hibernate.ddl-auto=update", "-jar", "/app.jar"]