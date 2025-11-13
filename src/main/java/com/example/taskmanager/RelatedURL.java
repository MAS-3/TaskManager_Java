package com.example.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn; // ★インポート
import jakarta.persistence.ManyToOne; // ★インポート
import jakarta.persistence.Column; // ★インポート

@Entity
public class RelatedURL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "Gihubブランチ", "figmaURL" など
    
    @Column(length = 1000) // URLは長くなる可能性がある
    private String url; // "http://..."

    /**
     * "多対1" の関連 (RelatedURL N : 1 Task)
     * このURLがどのタスクに属しているかを示します。
     */
    @ManyToOne
    @JoinColumn(name = "task_id") // DBには "task_id" カラムが作られます
    private Task task;

    // --- Getter / Setter ---
    // (JPAが動作するために必須です)

    // デフォルトコンストラクタ
    public RelatedURL() {
    }
    
    // name と url を受け取るコンストラクタ
    public RelatedURL(String name, String url) {
        this.name = name;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}