package com.example.taskmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "デザイン", "コーディング" など

    // --- Getter / Setter ---
    // (JPAが動作するために必須です)

    // デフォルトコンストラクタ
    public Genre() {
    }

    // name を受け取るコンストラクタ
    public Genre(String name) {
        this.name = name;
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
}