package com.example.taskmanager;

import jakarta.persistence.*;

@Entity
public class TaskImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 保存されたファイル名 (例: "abc-123.png")
    private String filename;

    // 元のファイル名 (例: "スクリーンショット.png") - 任意ですがあると便利
    private String originalFilename;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public TaskImage() {
    }

    public TaskImage(String filename, String originalFilename) {
        this.filename = filename;
        this.originalFilename = originalFilename;
    }

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}