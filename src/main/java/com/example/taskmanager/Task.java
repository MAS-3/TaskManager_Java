package com.example.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (H2/PostgreSQL/MySQLで推奨される設定)
    private Long id; // タスクのID

    /**
     * (4) タスクのタイトルを保存するカラム（フィールド）
     */
    private String title;

    /**
     * (5) タスクが完了したかどうかを保存するカラム（フィールド）
     * (「タスク完了ボタン - アーカイブに追加」の機能)
     * false = 未完了
     * true = 完了（アーカイブ行き）
     */
    private boolean isCompleted = false;

    /**
     * ★(新規追加) 完了した日時
     * （アーカイブの30日ルールなどで使う）
     */
    private LocalDateTime completedAt;

    /**
     * メモ欄（description）用のフィールドを追加
     * (「タスク概要」として使用)
     * @Column(length = 1000) を付けることで、
     * DB側で長めのテキスト（例: VARCHAR(1000)）を
     * 保存できるように指定します。
     */
    @Column(length = 2000) // 概要は長くなるかもしれないので 2000 に変更
    private String description;

    /**
     * ジャンルとの関連
     * @ManyToOne : 「多対一」の関係 (Task N : 1 Genre)
     */
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    /**
     * 納期との関連
     * @OneToMany : 「1対多」の関係 (Task 1 : N Deadline)
     * mappedBy = "task": 関連の管理は Deadline.java 側の "task" フィールドが行う
     * cascade = CascadeType.ALL: Taskを保存・削除したら、関連するDeadlineも一緒に保存・削除
     * orphanRemoval = true: TaskとDeadlineの関連が切れたら、DeadlineもDBから削除
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deadline> deadlines = new ArrayList<>();

    /**
     * 関連URLとの関連
     * @OneToMany : 「1対多」の関係 (Task 1 : N RelatedURL)
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatedURL> relatedUrls = new ArrayList<>();

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
     * 各フィールドの Getter と Setter
     */
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Genre getGenre() {
        return genre;
    }
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public List<Deadline> getDeadlines() {
        return deadlines;
    }
    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines = deadlines;
    }

    public List<RelatedURL> getRelatedUrls() {
        return relatedUrls;
    }
    public void setRelatedUrls(List<RelatedURL> relatedUrls) {
        this.relatedUrls = relatedUrls;
    }

    /**
     * 関連（Deadline, RelatedURL）を簡単に追加するための便利メソッド
     */
    public void addDeadline(Deadline deadline) {
        this.deadlines.add(deadline);
        deadline.setTask(this);
    }

    public void addRelatedURL(RelatedURL url) {
        this.relatedUrls.add(url);
        url.setTask(this);
    }

    /**
     * このタスクが持つ納期リスト(deadlines)の中で、
     * 「最も早い日付」を探して返す便利メソッド
     */
    public LocalDate getEarliestDeadlineDate() {
        if (deadlines.isEmpty()) {// もし納期が1つも登録されていなければ...
            return LocalDate.MAX; // ソート順で「一番後ろ」にしたいので、「すごく遠い未来」を返す
        }

        return deadlines.stream()// 納期リスト(deadlines)をストリーム（流れ）にして処理する
                .filter(d -> !d.isCompleted())//完了タスクのみを通す
                .map(Deadline::getDate) // Deadlineオブジェクトから「日付」だけを取り出す
                .min(LocalDate::compareTo) // 日付同士を比較して「最小値」を探す
                .orElse(LocalDate.MAX); // 万が一見つからなければ最大値を返す
    }
}