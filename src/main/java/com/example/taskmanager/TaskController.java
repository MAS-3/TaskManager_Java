package com.example.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // まとめてインポート
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.UUID;
import java.nio.file.*;
import java.io.IOException;

@Controller
public class TaskController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private RelatedURLRepository relatedURLRepository;
    @Autowired private TaskImageRepository taskImageRepository;
    
    
    // ★変更: DeadlineRepository -> TaskProcessRepository
    @Autowired private TaskProcessRepository taskProcessRepository;

    // --- 共通処理 ---
    private void loadTaskData(Model model) {
        var tasks = taskRepository.findByIsCompletedFalse();
        // ★変更: Task::getSortDate を使用
        tasks.sort(Comparator.comparing(Task::getSortDate));
        model.addAttribute("tasks", tasks);
    }

    @GetMapping("/")
    public String index() { return "redirect:/tasks"; }

    @GetMapping("/tasks")
    public String listTasks(Model model) {
        loadTaskData(model);
        model.addAttribute("allGenres", genreRepository.findAll());
        model.addAttribute("today", LocalDate.now());
        return "tasks";
    }

    @PostMapping("/tasks/create")
    public String createTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("genreId") Long genreId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,

            // 名前、開始日、終了日
            @RequestParam(value = "processName", required = false) List<String> processNames,
            @RequestParam(value = "processStartDate", required = false) List<String> processStartDates,
            @RequestParam(value = "processEndDate", required = false) List<String> processEndDates,

            @RequestParam(value = "urlName", required = false) List<String> urlNames,
            @RequestParam(value = "urlLink", required = false) List<String> urlLinks,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {
        Task newTask = new Task(title);
        newTask.setDescription(description);
        newTask.setStartDate(startDate);

        genreRepository.findById(genreId).ifPresent(newTask::setGenre);

        // ★変更: 工程(Process)の処理
        if (processNames != null && processStartDates != null && processEndDates != null) {
            for (int i = 0; i < processNames.size(); i++) {
                if (i >= processStartDates.size() || i >= processEndDates.size()) break;
                if (!processNames.get(i).isEmpty() && !processEndDates.get(i).isEmpty()) {
                    
                    TaskProcess process = new TaskProcess();
                    process.setName(processNames.get(i));
                    if (!processStartDates.get(i).isEmpty()) {
                        process.setStartDate(LocalDate.parse(processStartDates.get(i)));
                    }
                    process.setEndDate(LocalDate.parse(processEndDates.get(i)));
                    
                    newTask.addProcess(process); // ★変更
                }
            }
        }

        if (urlNames != null && urlLinks != null) {
            for (int i = 0; i < urlNames.size(); i++) {
                if (i >= urlLinks.size()) break;
                if (!urlNames.get(i).isEmpty() && !urlLinks.get(i).isEmpty()) {
                    RelatedURL url = new RelatedURL();
                    url.setName(urlNames.get(i));
                    url.setUrl(urlLinks.get(i));
                    newTask.addRelatedURL(url);
                }
            }
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                String storedFilename = saveImageFile(file);
                if (storedFilename != null) {
                    TaskImage taskImage = new TaskImage(storedFilename, file.getOriginalFilename());
                    newTask.addImage(taskImage);
                }
            }
        }
        taskRepository.save(newTask);
        return "redirect:/tasks";
    }

    // ... (completeTask, editTaskForm はほぼそのまま) ...

    @PostMapping("/tasks/{id}/complete")
    public String completeTask(@PathVariable("id") Long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setCompleted(true);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
        });
        return "redirect:/tasks";
    }
    
    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable("id") Long id, Model model) {
        var taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            model.addAttribute("task", taskOpt.get());
            model.addAttribute("allGenres", genreRepository.findAll());
            return "edit_task";
        }
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/update")
    public String updateTask(
            @PathVariable("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("genreId") Long genreId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,

            // ★変更: process...
            @RequestParam(value = "processName", required = false) List<String> processNames,
            @RequestParam(value = "processStartDate", required = false) List<String> processStartDates,
            @RequestParam(value = "processEndDate", required = false) List<String> processEndDates,

            @RequestParam(value = "urlName", required = false) List<String> urlNames,
            @RequestParam(value = "urlLink", required = false) List<String> urlLinks,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {
        var taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) return "redirect:/tasks";
        
        Task taskToUpdate = taskOpt.get();
        taskToUpdate.setTitle(title);
        taskToUpdate.setDescription(description);
        taskToUpdate.setStartDate(startDate);

        genreRepository.findById(genreId).ifPresent(taskToUpdate::setGenre);

        // ★変更: processes をクリア
        taskToUpdate.getProcesses().clear();
        taskToUpdate.getRelatedUrls().clear();

        // ★変更: 工程(Process)の処理
        if (processNames != null && processStartDates != null && processEndDates != null) {
            for (int i = 0; i < processNames.size(); i++) {
                if (i >= processStartDates.size() || i >= processEndDates.size()) break;
                if (!processNames.get(i).isEmpty() && !processEndDates.get(i).isEmpty()) {
                    
                    TaskProcess process = new TaskProcess();
                    process.setName(processNames.get(i));
                    if (!processStartDates.get(i).isEmpty()) {
                        process.setStartDate(LocalDate.parse(processStartDates.get(i)));
                    }
                    process.setEndDate(LocalDate.parse(processEndDates.get(i)));
                    
                    taskToUpdate.addProcess(process);
                }
            }
        }
        // ... (URL, 画像処理は createTask と同じなので省略) ...
        // (※本来は共通化すべきですが、今回は簡略化のためそのまま)
        
        if (urlNames != null && urlLinks != null) {
            for (int i = 0; i < urlNames.size(); i++) {
                if (i >= urlLinks.size()) break;
                if (!urlNames.get(i).isEmpty() && !urlLinks.get(i).isEmpty()) {
                    RelatedURL url = new RelatedURL();
                    url.setName(urlNames.get(i));
                    url.setUrl(urlLinks.get(i));
                    taskToUpdate.addRelatedURL(url);
                }
            }
        }
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                String storedFilename = saveImageFile(file);
                if (storedFilename != null) {
                    TaskImage taskImage = new TaskImage(storedFilename, file.getOriginalFilename());
                    taskToUpdate.addImage(taskImage);
                }
            }
        }

        taskRepository.save(taskToUpdate);
        return "redirect:/tasks";
    }

    // ... (delete, archive, revert はそのまま) ...
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable("id") Long id) {
        taskRepository.deleteById(id);
        return "redirect:/tasks";
    }
    @GetMapping("/archive")
    public String archiveList(Model model) {
        model.addAttribute("tasks", taskRepository.findByIsCompletedTrue());
        return "archive";
    }
    @PostMapping("/tasks/{id}/revert")
    public String revertTask(@PathVariable("id") Long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setCompleted(false);
            taskRepository.save(task);
        });
        return "redirect:/archive";
    }

    // ★変更: URLパスを /processes/... に変更
    @PostMapping("/processes/{id}/toggle")
    public String toggleProcess(@PathVariable("id") Long id, Model model) {
        taskProcessRepository.findById(id).ifPresent(p -> {
            p.setCompleted(!p.isCompleted());
            taskProcessRepository.save(p);
        });
        loadTaskData(model);
        return "tasks :: taskListArea";
    }
    
    // ... (deleteImage, saveImageFile はそのまま) ...
    @PostMapping("/images/{id}/delete")
    public String deleteImage(@PathVariable("id") Long id, Model model) {
        taskImageRepository.findById(id).ifPresent(image -> {
            try {
                Path filePath = Paths.get("/data/uploads").resolve(image.getFilename());
                Files.deleteIfExists(filePath);
            } catch (IOException e) { e.printStackTrace(); }
            taskImageRepository.delete(image);
        });
        loadTaskData(model);
        return "tasks :: taskListArea";
    }

    private String saveImageFile(MultipartFile file) {
        // (省略なしで以前のコードを使ってください)
        if (file.isEmpty()) return null;
        try {
            Path uploadDir = Paths.get("/data/uploads");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
            String originalFilename = file.getOriginalFilename();
            String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Files.copy(file.getInputStream(), uploadDir.resolve(storedFilename));
            return storedFilename;
        } catch (IOException e) { return null; }
    }
}