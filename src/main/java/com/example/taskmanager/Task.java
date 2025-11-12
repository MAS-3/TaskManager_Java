package com.example.taskmanager;

// jakarta.persistence.* の3つをインポートします
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * (1) @Entity
 * これがデータベースのテーブルに対応するクラス（エンティティ）
 * であることを示します。
 * （Spring Data JPAがこれを読み取って、自動で "task" テーブルを作成します）
 */
@Entity
public class Task {

    /**
     * (2) @Id
     * このフィールドがテーブルの「主キー（Primary Key）」であることを示します。
     */
    @Id
    /**
     * (3) @GeneratedValue
     * 主キー（id）の値をデータベースが自動で採番（例: 1, 2, 3...）
     * してくれることを示します。
     */
    @GeneratedValue
    private Long id; // タスクのID

    /**
     * (4) タスクのタイトルを保存するカラム（フィールド）
     */
    private String title;

    /**
     * (5) タスクが完了したかどうかを保存するカラム（フィールド）
     * 最初は必ず「未完了(false)」で作成されるようにデフォルト値を設定します。
     */
    private boolean completed = false;


    // --- (ここから下は、JPAやThymeleafが動作するために必要なお決まりの記述です) ---

    /**
     * (6) 引数なしのコンストラクタ
     * JPAがデータベースからデータを読み込む際に内部で使うため、必須です。
     */
    public Task() {
    }

    /**
     * (7) （便利のため）タイトルを指定してタスクを作成するためのコンストラクタ
     */
    public Task(String title) {
        this.title = title;
    }

    /**
     * (8) 各フィールドの Getter と Setter
     * JPAやThymeleafが、このクラスの内部データ（id, title, completed）に
     * アクセスするために必要です。
     *
     * (VSCodeの機能で自動生成もできます)
     */
    
    // id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    // title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // completed
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}