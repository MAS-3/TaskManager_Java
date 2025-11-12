package com.example.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * (1) @Repository
 * このインターフェースがデータベース操作を行う
 * 「リポジトリ（保管庫）」であることをSpringに伝えます。
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // (2) これだけ！

    /**
     * JpaRepository<Task, Long> を「extends（継承）」するだけで、
     * Spring Data JPAが裏側で自動的に以下のメソッドを実装してくれます。
     *
     * ・save(task)      : タスクの保存（新規作成 または 更新）
     * ・findAll()       : 全件検索（すべてのタスクを取得）
     * ・findById(id)    : 1件検索（IDでタスクを取得）
     * ・deleteById(id)  : 1件削除（IDでタスクを削除）
     *
     * <Task, Long> の意味は、
     * 「Task（Taskエンティティ）を扱います」
     * 「その主キーの型は Long（Task.javaで id を Long にしたため）です」
     * という指定です。
     */
}