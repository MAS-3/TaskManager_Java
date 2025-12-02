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
import java.util.Comparator;
import org.springframework.web.multipart.MultipartFile; // ファイル受け取り用
import java.nio.file.Files;   // ファイル書き込み用
import java.nio.file.Path;    // パス操作用
import java.nio.file.Paths;   // パス操作用
import java.util.UUID;        // ユニークなファイル名生成用
import java.io.IOException;   // エラー処理用


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

    @Autowired
    private TaskImageRepository taskImageRepository;


    @GetMapping("/")
    public String index() {
        return "redirect:/tasks";
    }

    /**
     * (3) @GetMapping("/tasks")
     * ブラウザから "http://localhost:8080/tasks" というURLへGETリクエストが来た時に、この listTasks メソッドを実行する、という設定です。
     * (Laravelの routes/web.php で Route::get('/tasks', ...) と書くのと同じです)
     */
    @GetMapping("/tasks")
    public String listTasks(Model model) {

        // ★共通メソッドを呼ぶ
        loadTaskData(model);

        //タスクリストの取得
        // (A) DBから「未完了(isCompleted = false)」のタスクのみを取得する（アーカイブ用）
        var tasks = taskRepository.findByIsCompletedFalse(); // ★ Repositoryに追加したメソッドを使う

        tasks.sort(Comparator.comparing(Task::getEarliestDeadlineDate));//納期が近い順にソート

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
        // 基本情報を受け取る
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("genreId") Long genreId,

        //「複数の」納期 (配列として受け取る)
        @RequestParam(value = "deadlineName", required = false) List<String> deadlineNames,
        @RequestParam(value = "deadlineDate", required = false) List<String> deadlineDates,

        //「複数の」関連URL (配列として受け取る)
        @RequestParam(value = "urlName", required = false) List<String> urlNames,
        @RequestParam(value = "urlLink", required = false) List<String> urlLinks,

        // 画像ファイルを受け取る (name="imageFiles")
        @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles
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

        // --- 納期入力の処理 ---
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

        // --- 関連URL入力の処理 ---
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

        // --- 画像の保存処理 ---
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                // さっき作った便利メソッドで保存
                String storedFilename = saveImageFile(file);
                
                if (storedFilename != null) {
                    // DBに保存するための TaskImage エンティティを作成
                    TaskImage taskImage = new TaskImage(storedFilename, file.getOriginalFilename());
                    newTask.addImage(taskImage); // Taskに関連付け
                }
            }
        }

        // セットしたもの一式を保存
        taskRepository.save(newTask);
        // タスク一覧ページ ("/tasks") にリダイレクト（再表示）
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

            // フォームから送られてくる基本情報
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("genreId") Long genreId,

            // 「複数の」納期 (配列として受け取る)
            @RequestParam(value = "deadlineName", required = false) List<String> deadlineNames,
            @RequestParam(value = "deadlineDate", required = false) List<String> deadlineDates,

            // 「複数の」関連URL (配列として受け取る)
            @RequestParam(value = "urlName", required = false) List<String> urlNames,
            @RequestParam(value = "urlLink", required = false) List<String> urlLinks,

            //画像の保存
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {

        // (1)更新対象のタスクをDBから探す
        var taskOpt = taskRepository.findById(id);

        // もしタスクが見つからなければ、何もせず一覧に戻る
        if (taskOpt.isEmpty()) {
            return "redirect:/tasks";
        }

        // (2) 更新するタスク本体を取り出す
        Task taskToUpdate = taskOpt.get(); 

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

        // --- 画像の追加処理 (既存の画像は消さずに、追加だけする仕様にします) ---
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                String storedFilename = saveImageFile(file);
                if (storedFilename != null) {
                    TaskImage taskImage = new TaskImage(storedFilename, file.getOriginalFilename());
                    taskToUpdate.addImage(taskImage); // 既存のリストに追加
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

    /**
     * (12) ★納期の完了状態を切り替える (Toggle)
     * (POST /deadlines/{id}/toggle)
     * ボタンを押すたびに true <-> false が入れ替わります。
     */
    @PostMapping("/deadlines/{id}/toggle")
    public String toggleDeadline(@PathVariable("id") Long id ,Model model) {
        deadlineRepository.findById(id).ifPresent(d -> {
            // 現在の状態を反転させる (trueならfalseに、falseならtrueに)
            d.setCompleted(!d.isCompleted());
            deadlineRepository.save(d);
        });

        // (1) 最新のタスクデータを取得・ソートしてModelに入れる
        loadTaskData(model);

        // (2) ★ページ全体("tasks")ではなく、
        //      tasks.htmlの中にある "taskListArea" という断片だけを返す！
        return "tasks :: taskListArea";
    }

    /**
     * DBから未完了タスクを取得し、ソートしてModelに入れる共通処理
     */
    private void loadTaskData(Model model) {
        var tasks = taskRepository.findByIsCompletedFalse();
        // ソート（Task.javaで実装したロジック）
        tasks.sort(Comparator.comparing(Task::getEarliestDeadlineDate));
        
        model.addAttribute("tasks", tasks);
        // (ジャンル一覧などはリスト更新だけなら不要なので省略可ですが、念のため入れてもOK)
    }

    /**
     * 画像ファイルを保存し、保存されたファイル名を返す
     */
    private String saveImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // (1) 保存先ディレクトリ (/data/uploads) を作成
            //     Dockerのボリウムマウント先(/data)の下に作るので、消えません。
            Path uploadDir = Paths.get("/data/uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // (2) ファイル名が被らないように UUID をつける
            //     (例: "my-image.png" -> "a0eebc99-9c0b..._my-image.png")
            String originalFilename = file.getOriginalFilename();
            String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // (3) ファイルを保存
            Path destinationPath = uploadDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), destinationPath);

            return storedFilename; // 保存したファイル名を返す

        } catch (IOException e) {
            e.printStackTrace(); // エラー時はログに出す
            return null;
        }
    }

    /**
     * (13) ★画像の削除処理 (HTMX対応)
     * (POST /images/{id}/delete)
     * ファイルシステムからファイルを消し、DBからも削除します。
     */
    @PostMapping("/images/{id}/delete")
    public String deleteImage(@PathVariable("id") Long id, Model model) {
        
        // IDから画像データを検索
        taskImageRepository.findById(id).ifPresent(image -> {
            
            // (A) 実際のファイルを削除 (try-catchで安全に)
            try {
                Path filePath = Paths.get("/data/uploads").resolve(image.getFilename());
                Files.deleteIfExists(filePath); // ファイルがあれば消す
            } catch (IOException e) {
                e.printStackTrace(); // エラーならログに出すだけで処理は続ける
            }

            // (B) データベースから削除
            taskImageRepository.delete(image);
        });

        // (C) 最新のタスク一覧を取得して画面を更新 (HTMX)
        loadTaskData(model);
        return "tasks :: taskListArea";
    }
}