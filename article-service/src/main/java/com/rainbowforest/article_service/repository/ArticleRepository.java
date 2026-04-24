package com.rainbowforest.article_service.repository;

import com.rainbowforest.article_service.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // Sắp xếp bài viết mới nhất lên đầu
    List<Article> findAllByOrderByCreatedDateDesc();
}