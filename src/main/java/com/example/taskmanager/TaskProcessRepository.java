package com.example.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskProcessRepository extends JpaRepository<TaskProcess, Long> {
}