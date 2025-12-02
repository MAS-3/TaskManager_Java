package com.example.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatedURLRepository extends JpaRepository<RelatedURL, Long> {
    // これだけで RelatedURL の save, findAll などが使えます
}