package com.example.taskmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GenreController {

    @Autowired
    private GenreRepository genreRepository;

    // ジャンル一覧画面を表示
    @GetMapping("/genres")
    public String listGenres(Model model) {
        model.addAttribute("genres", genreRepository.findAll());
        return "genres";
    }

    // ジャンルの新規作成
    @PostMapping("/genres/create")
    public String createGenre(@RequestParam("name") String name) {
        if (!name.isEmpty()) {
            Genre genre = new Genre();
            genre.setName(name);
            genreRepository.save(genre);
        }
        return "redirect:/genres";
    }

    // ジャンルの削除
    // (※そのジャンルを使っているタスクがある場合、エラーになる可能性がありますが、まずはシンプルに実装します)
    @PostMapping("/genres/{id}/delete")
    public String deleteGenre(@PathVariable("id") Long id) {
        genreRepository.deleteById(id);
        return "redirect:/genres";
    }
}