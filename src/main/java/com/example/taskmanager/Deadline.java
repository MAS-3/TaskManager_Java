package com.example.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "キーになる期限", "タスクの納期" など
    
    private LocalDate startDate; // 開始日
    private LocalDate endDate;   // 終了日

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // デフォルトコンストラクタ
    public Deadline() {
    }

    // name と date を受け取るコンストラクタ
    public Deadline(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    // 開始日
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    // 終了日
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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