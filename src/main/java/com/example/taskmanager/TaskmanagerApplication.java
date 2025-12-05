package com.example.taskmanager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskmanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskmanagerApplication.class, args);
    }

    /**
     * (1) アプリケーション起動時に、この @Bean メソッドが自動で実行されます
     * (2) GenreRepository を引数で受け取ります (Springが自動で渡してくれます)
     */
    @Bean // ← ★ @Bean を import する
    public CommandLineRunner loadInitialData(GenreRepository genreRepository) { // ← ★ GenreRepository を import する
        return (args) -> {
            
            // (3) もしDBにジャンルが1件も登録されていなかったら...
            if (genreRepository.count() == 0) {
                System.out.println("===== ジャンルの初期データを登録します =====");
                
                // (4) 機能リストにあった3つのジャンルを
                //     Genreオブジェクトとして作成
                Genre design = new Genre("デザイン");
                Genre coding = new Genre("コーディング");
                Genre illustration = new Genre("イラスト制作");

                // (5) 3つをまとめてDBに保存する
                genreRepository.saveAll(List.of(design, coding, illustration)); // ← ★ List を import する
                
                System.out.println("===== ジャンルの登録が完了しました =====");
            }
        };
    }
}