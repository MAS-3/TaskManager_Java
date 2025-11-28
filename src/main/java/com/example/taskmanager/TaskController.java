package com.example.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * TaskController
 * ========================================
 * 
 * タスク管理アプリケーションのメインコントローラー
 * ブラウザからのリクエストを受け付け、DBとやり取りし、HTMLを返す
 * 
 * 【エンドポイント一覧】
 * - GET  /tasks              : 未完了タスク一覧を表示
 * - POST /tasks/create       : 新規タスク作成
 * - POST /tasks/{id}/complete: タスク完了（アーカイブへ）
 * - POST /tasks/{id}/delete  : タスク削除
 * - GET  /archive            : 完了済みタスク一覧を表示
 * - GET  /tasks/{id}/edit    : タスク編集画面を表示
 */
@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private DeadlineRepository deadlineRepository;

    @Autowired
    private RelatedURLRepository relatedURLRepository;

    /**
     * (3) @GetMapping("/tasks")
     * ブラウザから "http://localhost:8080/tasks" というURLへGETリクエストが来た時に、この listTasks メソッドを実行する、という設定です。
     * (Laravelの routes/web.php で Route::get('/tasks', ...) と書くのと同じです)
     */
    @GetMapping("/tasks")
    public String listTasks(Model model) { // (4) Model model

        //タスクリストの取得
        // (A) DBから「未完了(isCompleted = false)」のタスクのみを取得する（アーカイブ用）
        var tasks = taskRepository.findByIsCompletedFalse(); // ★ Repositoryに追加したメソッドを使う
        // (B) DBから「ジャンル」の全リストを取得する
        var allGenres = genreRepository.findAll();

        // (6) 取得したタスク一覧を "tasks" という名前で Model に追加する
        // Model とは、コントローラからHTML（画面）へデータを運ぶための「かばん」です。
        model.addAttribute("tasks", tasks);
        model.addAttribute("allGenres", allGenres);//"allGenres" もかばんに入れる
        model.addAttribute("today", LocalDate.now());

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
        // createTaskメソッドの引数
        // (A) 基本情報を受け取る
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("genreId") Long genreId,

        // (B) ★「複数の」納期 (配列として受け取る)
        @RequestParam(value = "deadlineName", required = false) List<String> deadlineNames,
        @RequestParam(value = "deadlineDate", required = false) List<String> deadlineDates,

        // (C) ★「複数の」関連URL (配列として受け取る)
        @RequestParam(value = "urlName", required = false) List<String> urlNames,
        @RequestParam(value = "urlLink", required = false) List<String> urlLinks
    ) {
        // (2) @RequestParam("title") String title
        // フォームから送られてきた name="title" のデータを、
        // String 型の変数 title として受け取ります。

        // (3) 受け取ったタイトルで、新しい Task オブジェクトを作成
        Task newTask = new Task(title);
        newTask.setTitle(title);//タイトルをセット
        newTask.setDescription(description);//概要をセット
        
        genreRepository.findById(genreId).ifPresent(genre -> {//ユーザーがブラウザ上で選択したジャンルを取得
            newTask.setGenre(genre);//ジャンルをセット
        });

        // 納期入力の処理
        if (deadlineNames != null && deadlineDates != null) {
            for (int i = 0; i < deadlineNames.size(); i++) {

                // ★【安全装置】日付リストが名前リストより短い場合、エラーにならないようにループを抜ける
                if (i >= deadlineDates.size()) break;

                if (!deadlineNames.get(i).isEmpty() && !deadlineDates.get(i).isEmpty()) {
                    Deadline deadline = new Deadline();
                    deadline.setName(deadlineNames.get(i));
                    deadline.setDate(LocalDate.parse(deadlineDates.get(i)));
                    
                    // ★Task.java の便利メソッドを使って関連付ける
                    newTask.addDeadline(deadline); 
                }
            }
        };

        // (4) 関連URL入力の処理
        if (urlNames != null && urlLinks != null) {
            for (int i = 0; i < urlNames.size(); i++) {
                // ★【安全装置】リンク先リストが名前リストより短い場合、ループを抜ける
                if (i >= urlLinks.size()) break;

                if (!urlNames.get(i).isEmpty() && !urlLinks.get(i).isEmpty()) {
                    RelatedURL url = new RelatedURL();
                    url.setName(urlNames.get(i));
                    url.setUrl(urlLinks.get(i));
                    
                    newTask.addRelatedURL(url);
                }
            }
        };


        // セットしたもの一式を保存
        taskRepository.save(newTask);

        // (5) 処理が終わったら、タスク一覧ページ ("/tasks") にリダイレクト（再表示）
        return "redirect:/tasks";
    }


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
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task); // (6) 変更したタスクをDBに上書き保存 (IDがあるのでUPDATEになる)
        }
        
        // (7) 一覧ページにリダイレクト
        return "redirect:/tasks";
    }


    /**
     * (8) ★タスク編集画面の表示 (GET /tasks/{id}/edit)
     * 編集するタスクの情報と、ジャンル一覧を画面に渡します。
     */
    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable("id") Long id, Model model) {
        
        var taskOpt = taskRepository.findById(id);// (A) IDでタスクを検索

        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());// (B) もしタスクが見つかったら、Model（かばん）に入れる
            model.addAttribute("allGenres", genreRepository.findAll());// (C) 「ジャンル」の全リストもModelに入れる（ドロップダウンリスト用）
            return "edit_task";// (D) edit_task.html を表示

        } else {
            return "redirect:/tasks";// (E) もしタスクが見つからなかったら、一覧画面に強制的に戻す

        }
    }


    /**
     * (9) ★タスク更新処理
     * (POST /tasks/{id}/update)
     * 編集画面のフォームから送られたデータで、タスクを上書き保存します。
     */
    @PostMapping("/tasks/{id}/update")
    public String updateTask(
            @PathVariable("id") Long id, // (A) どのタスクを更新するか (URLからID取得)

            // (B) フォームから送られてくる基本情報
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("genreId") Long genreId,

            // (C) ★「複数の」納期 (配列として受け取る)
            @RequestParam(value = "deadlineName", required = false) List<String> deadlineNames,
            @RequestParam(value = "deadlineDate", required = false) List<String> deadlineDates,

            // (D) ★「複数の」関連URL (配列として受け取る)
            @RequestParam(value = "urlName", required = false) List<String> urlNames,
            @RequestParam(value = "urlLink", required = false) List<String> urlLinks
    ) {

        // (1) まず、更新対象のタスクをDBから探す
        var taskOpt = taskRepository.findById(id);

        if (taskOpt.isEmpty()) {
            return "redirect:/tasks";// もしタスクが見つからなければ、何もせず一覧に戻る
        }

        Task taskToUpdate = taskOpt.get(); // (2) 更新するタスク本体を取り出す

        // (3) 基本情報を上書きセット
        taskToUpdate.setTitle(title);
        taskToUpdate.setDescription(description);

        // (4) ジャンル（関連）を上書きセット
        genreRepository.findById(genreId).ifPresent(genre -> {
            taskToUpdate.setGenre(genre);
        });

        // (5) ★ 既存の「納期」と「URL」を一度すべてクリアする
        // (これが一番簡単な「更新」方法です)
        taskToUpdate.getDeadlines().clear();
        taskToUpdate.getRelatedUrls().clear();
        // (注意: orphanRemoval=true のおかげで、リストから消すだけでDBからも削除されます)

        // (6) フォームから送られてきた「新しい納期」を処理（createTaskと同じロジック）
        if (deadlineNames != null && deadlineDates != null) {
            for (int i = 0; i < deadlineNames.size(); i++) {

                // ★【安全装置】日付リストが名前リストより短い場合、エラーにならないようにループを抜ける
                if (i >= deadlineDates.size()) break;

                if (!deadlineNames.get(i).isEmpty() && !deadlineDates.get(i).isEmpty()) {
                    Deadline deadline = new Deadline();
                    deadline.setName(deadlineNames.get(i));
                    deadline.setDate(LocalDate.parse(deadlineDates.get(i)));
                    taskToUpdate.addDeadline(deadline); // 新しい納期として追加
                }
            }
        }

        // (7) フォームから送られてきた「新しいURL」を処理（createTaskと同じロジック）
        if (urlNames != null && urlLinks != null) {
            for (int i = 0; i < urlNames.size(); i++) {

                // ★【安全装置】リンク先リストが名前リストより短い場合、ループを抜ける
                if (i >= urlLinks.size()) break;

                if (!urlNames.get(i).isEmpty() && !urlLinks.get(i).isEmpty()) {
                    RelatedURL url = new RelatedURL();
                    url.setName(urlNames.get(i));
                    url.setUrl(urlLinks.get(i));
                    taskToUpdate.addRelatedURL(url); // 新しいURLとして追加
                }
            }
        }

        // (8) ★ 変更をDBに保存 (IDがあるのでINSERTではなくUPDATEが実行される)
        taskRepository.save(taskToUpdate);

        // (9) 一覧ページにリダイレクト
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

    //
    //
    // Archive
    //
    //
    /**
     * (10) ★アーカイブ画面の表示 (実装)
     * (GET /archive)
     */
    @GetMapping("/archive")
    public String archiveList(Model model) {
        // (A) 完了済み(isCompleted = true)のタスクを取得
        var archiveTasks = taskRepository.findByIsCompletedTrue();
        
        model.addAttribute("tasks", archiveTasks);
        
        return "archive"; // archive.html を表示
    }

    /**
     * (11) ★タスクを未完了に戻す処理 (Revert)
     * (POST /tasks/{id}/revert)
     */
    @PostMapping("/tasks/{id}/revert")
    public String revertTask(@PathVariable("id") Long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setCompleted(false); // 未完了に戻す
            taskRepository.save(task);
        });
        return "redirect:/archive"; // アーカイブ一覧に戻る
    }
}