package com.rainbowforest.article_service.controller;

import com.rainbowforest.article_service.entity.Article;
import com.rainbowforest.article_service.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    // Lấy tất cả bài viết
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleRepository.findAllByOrderByCreatedDateDesc());
    }

    // Lấy chi tiết 1 bài viết theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo bài viết mới
    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleRepository.save(article));
    }

    // Cập nhật bài viết
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article articleDetails) {
        return articleRepository.findById(id).map(article -> {
            article.setTitle(articleDetails.getTitle());
            article.setShortDescription(articleDetails.getShortDescription());
            article.setContent(articleDetails.getContent());
            article.setAuthor(articleDetails.getAuthor());
            article.setThumbnailUrl(articleDetails.getThumbnailUrl());
            return ResponseEntity.ok(articleRepository.save(article));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Xóa bài viết
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}