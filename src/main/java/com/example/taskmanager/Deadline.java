package com.example.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn; // ★インポート
import jakarta.persistence.ManyToOne; // ★インポート
import java.time.LocalDate; // ★インポート

@Entity
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "キーになる期限", "タスクの納期" など
    
    private LocalDate date; // 期限日

    /**
     * "多対1" の関連 (Deadline N : 1 Task)
     * この納期がどのタスクに属しているかを示します。
     * @ManyToOne : 「多（Deadline）対 一（Task）」の関係
     */
    @ManyToOne
    @JoinColumn(name = "task_id") // DBには "task_id" カラムが作られます
    private Task task;
    private boolean isCompleted = false;//完了ステータス管理カラム

    // --- Getter / Setter ---
    // (JPAが動作するために必須です)
    
    // デフォルトコンストラクタ
    public Deadline() {
    }

    // name と date を受け取るコンストラクタ
    public Deadline(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    // Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted){
        this.isCompleted = isCompleted;
    }
}