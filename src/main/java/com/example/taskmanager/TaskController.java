package com.example.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
// ★1. この import 文を追記してください
import org.springframework.web.bind.annotation.PathVariable;

/**
 * (1) @Controller
 * これがブラウザからのリクエストを受け付ける「コントローラ（司令塔）」
 * であることをSpringに伝えます。
 *
 * (メモ: APIでデータを返すときは @RestController を使いましたが、
 * 今回はHTML画面を返すので @Controller を使います)
 */
@Controller
public class TaskController {

    /**
     * (2) @Autowired (オートワイヤード)
     * Springが自動で作ってくれた TaskRepository の実体を、
     * この変数（taskRepository）に自動でセット（注入）してね、という指示です。
     * これにより、このクラス内で taskRepository.findAll() などが使えるようになります。
     */
    @Autowired
    private TaskRepository taskRepository;

    /**
     * (3) @GetMapping("/tasks")
     * ブラウザから "http://localhost:8080/tasks" というURLへ
     * GETリクエストが来た時に、この listTasks メソッドを実行する、という設定です。
     * (Laravelの routes/web.php で Route::get('/tasks', ...) と書くのと同じです)
     */
    @GetMapping("/tasks")
    public String listTasks(Model model) { // (4) Model model

        // (5) DBからすべてのタスクを取得する
        // TaskRepository の findAll() メソッドを使います。
        var tasks = taskRepository.findAll();

        // (6) 取得したタスク一覧を "tasks" という名前で Model に追加する
        // Model とは、コントローラからHTML（画面）へデータを運ぶための「かばん」です。
        model.addAttribute("tasks", tasks);

        // (7) "tasks.html" という名前のHTMLテンプレートを表示してね、と返す
        // Spring Boot（Thymeleaf）は自動的に
        // "src/main/resources/templates/tasks.html" を探しに行きます。
        return "tasks";
    }

    /**
     * (1) @PostMapping("/tasks/create")
     * HTMLフォームの th:action と method="post" に対応します。
     * "/tasks/create" へのPOSTリクエストが来たら、このメソッドが動きます。
     */
    @PostMapping("/tasks/create")
    public String createTask(
        @RequestParam("title") String title,
        @RequestParam("description") String description
    ) {
        // (2) @RequestParam("title") String title
        // フォームから送られてきた name="title" のデータを、
        // String 型の変数 title として受け取ります。

        // (3) 受け取ったタイトルで、新しい Task オブジェクトを作成
        Task newTask = new Task(title);

        // ★★★ 2. 受け取った description を newTask にセットする ★★★
        newTask.setDescription(description);

        // (4) TaskRepository を使って、新しいタスクをDBに保存
        taskRepository.save(newTask);

        // (5) 処理が終わったら、タスク一覧ページ ("/tasks") にリダイレクト（再表示）
        //    (Laravel の redirect('/tasks') と同じです)
        return "redirect:/tasks";
    }

    // --- ★ここから下が追記分 (Update / Delete) ---

    /**
     * (1) 完了 (Update) 処理
     * @PostMapping("/tasks/{id}/complete")
     * HTMLの th:action="@{/tasks/{id}/complete(id=${task.id})}" に対応します。
     */
    @PostMapping("/tasks/{id}/complete")
    public String completeTask(@PathVariable("id") Long id) {
        // (2) @PathVariable("id") Long id
        // URL (/tasks/1/complete) の "1" の部分（パス変数）を、
        // Long 型の変数 id として受け取ります。
        // (@RequestParam がフォームデータを扱うのに対し、@PathVariable はURLのパスを扱います)

        // (3) DBからタスクをIDで検索
        // findById は Optional<Task> という「見つからないかもしれない」型で返す
        var taskOpt = taskRepository.findById(id);

        // (4) もしタスクが見つかったら (Optional の中身があったら)
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get(); // タスク本体を取り出す
            task.setCompleted(true); // (5) 完了フラグを true に変更
            taskRepository.save(task); // (6) 変更したタスクをDBに上書き保存 (IDがあるのでUPDATEになる)
        }
        
        // (7) 一覧ページにリダイレクト
        return "redirect:/tasks";
    }

    /**
     * (2) 削除 (Delete) 処理
     * @PostMapping("/tasks/{id}/delete")
     * HTMLの th:action="@{/tasks/{id}/delete(id=${task.id})}" に対応します。
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable("id") Long id) {
        // (1) URLから受け取ったIDを使って、DBからタスクを削除
        taskRepository.deleteById(id);
        
        // (2) 一覧ページにリダイレクト
        return "redirect:/tasks";
    }
}