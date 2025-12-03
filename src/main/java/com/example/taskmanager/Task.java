package com.example.taskmanager;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private boolean isCompleted = false;
    private LocalDate startDate;
    private LocalDateTime completedAt;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    /**
     * ★変更: Deadline -> TaskProcess
     * 変数名も deadlines -> processes に変更
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskProcess> processes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelatedURL> relatedUrls = new LinkedHashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskImage> images = new LinkedHashSet<>();

    public Task() {}
    public Task(String title) { this.title = title; }
    
    // --- Getter / Setter ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    // ★変更: processes の Getter/Setter
    public Set<TaskProcess> getProcesses() { return processes; }
    public void setProcesses(Set<TaskProcess> processes) { this.processes = processes; }

    public Set<RelatedURL> getRelatedUrls() { return relatedUrls; }
    public void setRelatedUrls(Set<RelatedURL> relatedUrls) { this.relatedUrls = relatedUrls; }

    public Set<TaskImage> getImages() { return images; }
    public void setImages(Set<TaskImage> images) { this.images = images; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

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
     * ★名前変更: getEarliestDeadlineDate -> getSortDate (汎用的にしました)
     */
    public LocalDate getSortDate() {
        if (processes.isEmpty()) {
            return LocalDate.MAX; 
        }
        return processes.stream()
                .filter(p -> !p.isCompleted())
                .map(TaskProcess::getEndDate) // Processの終了日で比較
                .min(LocalDate::compareTo)
                .orElse(LocalDate.MAX);
    }
}