package com.example.taskmanager;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
public class Task {
    //id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //タスクタイトル
    private String title;

    //タスクステータス（完了ボタン）
    private boolean isCompleted = false;
    private LocalDateTime completedAt;

    //タスク開始日
    private LocalDate startDate;

    //タスク終了日
    private LocalDate endDate;

    //概要
    @Column(length = 2000)
    private String description;

    //ジャンル
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    //工程
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate ASC, id ASC")
    private Set<TaskProcess> processes = new LinkedHashSet<>();

    //関係URL
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelatedURL> relatedUrls = new LinkedHashSet<>();

    //画像
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskImage> images = new LinkedHashSet<>();

    /*
    * --- メソッド ---
    */

    //コンストラクタ
    public Task() {}
    public Task(String title) { this.title = title; }


    /*
    * --- Getter / Setter ---
    */
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    //タイトル
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    //完了ステータス
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    //概要
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    //ジャンル
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    // 工程
    public Set<TaskProcess> getProcesses() { return processes; }
    public void setProcesses(Set<TaskProcess> processes) { this.processes = processes; }

    // 関係URL
    public Set<RelatedURL> getRelatedUrls() { return relatedUrls; }
    public void setRelatedUrls(Set<RelatedURL> relatedUrls) { this.relatedUrls = relatedUrls; }

    // 画像
    public Set<TaskImage> getImages() { return images; }
    public void setImages(Set<TaskImage> images) { this.images = images; }

    // タスク開始日、終了日
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // --- 便利メソッド ---

    // ★変更: addProcess
    public void addProcess(TaskProcess process) {
        this.processes.add(process);
        process.setTask(this);
    }

    public void addRelatedURL(RelatedURL url) {
        this.relatedUrls.add(url);
        url.setTask(this);
    }

    public void addImage(TaskImage image) {
        this.images.add(image);
        image.setTask(this);
    }

    /**
     * ソート用メソッド
     * タスク自身の endDate があればそれを優先、なければ工程から計算
     */
    public LocalDate getSortDate() { // (旧 getEarliestDeadlineDate)
        // 1. タスク自体の終了日があれば、それをソート基準にする
        if (this.endDate != null) {
            return this.endDate;
        }

        // 2. なければ、未完了の工程の中で一番早い日を使う (既存ロジック)
        if (processes.isEmpty()) {
            return LocalDate.MAX; 
        }
        return processes.stream()
                .filter(p -> !p.isCompleted())
                .map(TaskProcess::getEndDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.MAX);
    }
}