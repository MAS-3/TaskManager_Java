# TaskManager (Java)

Spring BootとDockerで作成したシンプルなタスク管理アプリです。

## 必要なもの

* [Docker Desktop](https://www.docker.com/products/docker-desktop/)

（開発モードで実行する場合は、**Visual Studio Code** と **[Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)** 拡張機能も必要です）

---

## 実行方法

このリポジトリには2つの実行方法があります。

### 1. 運用モード (VSCode不要、実行のみ)

アプリをローカルで（本番のように）起動するだけの場合の手順です。

1.  **リポジトリをクローン:**
    ```bash
    git clone [https://github.com/MAS-3/TaskManager_Java.git](https://github.com/MAS-3/TaskManager_Java.git)
    cd TaskManager_Java
    ```

2.  **Dockerイメージをビルド:**
    （`taskmanager-app` という名前のイメージを作成します）
    ```bash
    docker build -t taskmanager-app .
    ```

3.  **Dockerコンテナを実行:**
    （ホストの`8080`番ポートに接続し、`taskmanager-db`フォルダにデータを永続化します）

    ```bash
    docker run -d -p 3000:3000 -e SPRING_PROFILES_ACTIVE=[環境名を指定：例: sht-macbook] -v $(pwd) taskmanager-db:/data --name my-task-app taskmanager-app
    # 例: sht-macbookの設定で起動
        docker run -d -p 3000:3000 -e SPRING_PROFILES_ACTIVE=sht-macbook -v $(pwd)/taskmanager-db:/data --name my-task-app taskmanager-app
    ```
    （相対パスがローカル環境で変わるので、(pwd)で実行

5.  **アクセス:**
    ブラウザで `http://localhost:8080/tasks` を開きます。

---

### 2. 開発モード (VSCodeでコードを編集する場合)

VSCodeとDev Containersを使って、コンテナ内で開発・実行する場合の手順です。

1.  **リポジトリをクローン:**
    ```bash
    git clone [https://github.com/MAS-3/TaskManager_Java.git](https://github.com/MAS-3/TaskManager_Java.git)
    ```

2.  **VSCodeでフォルダを開く:**
    VSCodeで `TaskManager_Java` フォルダを開きます。

3.  **コンテナで開き直す:**
    * VSCodeの右下に「Reopen in Container（コンテナで開き直す）」という通知が表示されるので、それをクリックします。
    * （もし通知が出なければ、コマンドパレット（`Ctrl+Shift+P`）を開き、`Dev Containers: Reopen in Container` を実行します）

4.  **アプリを実行:**
    コンテナの起動が完了したら（VSCodeの左下が「開発コンテナー」表示になります）、`src/main/java/.../TaskmanagerApplication.java` を開き、`main` メソッドの上にある「**Run**」をクリックします。

5.  **アクセス:**
    ブラウザで `http://localhost:8080/tasks` を開きます。

### 再ビルドコマンド
``` 
コンテナを止める
docker stop my-task-app
コンテナ削除
docker rm my-task-app
コンテナをビルド
docker build -t taskmanager-app . (キャッシュあり)
docker build --no-cache -t taskmanager-app . (キャッシュなし)
コンテナを起動（port:8080）
docker run -d -p 8080:8080 -v ./taskmanager-db:/data --name my-task-app taskmanager-app
```

### データベースの削除
#### Mac / Linux
```　rm -rf ./taskmanager-db　```
#### Windows (PowerShell)
``` rmdir -r -fo ./taskmanager-db　```


### H2ブラウザログイン情報 2025.12.4(追記)
```
JDBC URL: jdbc:h2:file:/data/taskdb
（Dockerfileで指定した、コンテナ内のDBファイルパスです）
User Name: sa
（H2のデフォルトユーザー名です）
Password: （空のまま）
（H2のデフォルトパスワードは空です）
```

### Basic認証 (AWSアップロード仕様) 2025.12.4(追記)
** ログイン情報 **
> UseerName : shota.masuda
> Password : ac1951224
