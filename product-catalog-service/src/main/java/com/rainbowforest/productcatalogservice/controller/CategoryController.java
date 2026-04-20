package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Category;
import com.rainbowforest.productcatalogservice.repository.CategoryRepository;
import com.rainbowforest.productcatalogservice.service.AuditLogClient; // 🔥 IMPORT BƯU TÁ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired(required = false) // 🔥 CẤY BƯU TÁ
    private AuditLogClient auditLogClient;

    // Lấy toàn bộ danh mục
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
    }

    // Thêm danh mục mới
    @PostMapping
    public ResponseEntity<Category> add(
            @RequestBody Category category,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        Category savedCat = categoryRepository.save(category);

        // 🚀 BẮN LOG: THÊM DANH MỤC (Dùng tên thật)
        if (auditLogClient != null) {
            auditLogClient.sendLog(userName, "CREATE", "PRODUCT-SERVICE",
                    "Đã thêm danh mục mới: " + savedCat.getCategoryName());
        }

        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    // Sửa danh mục
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(
            @PathVariable Long id,
            @RequestBody Category details,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        return categoryRepository.findById(id).map(cat -> {
            cat.setCategoryName(details.getCategoryName());
            cat.setDescription(details.getDescription());
            Category updatedCat = categoryRepository.save(cat);

            // 🚀 BẮN LOG: SỬA DANH MỤC (Dùng tên thật)
            if (auditLogClient != null) {
                auditLogClient.sendLog(userName, "UPDATE", "PRODUCT-SERVICE",
                        "Cập nhật danh mục: " + updatedCat.getCategoryName());
            }

            return new ResponseEntity<>(updatedCat, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        categoryRepository.deleteById(id);

        // 🚀 BẮN LOG: XÓA DANH MỤC (Dùng tên thật)
        if (auditLogClient != null) {
            auditLogClient.sendLog(userName, "DELETE", "PRODUCT-SERVICE", "Đã xóa danh mục có ID: " + id);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}