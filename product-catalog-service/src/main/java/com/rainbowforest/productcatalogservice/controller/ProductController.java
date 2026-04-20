package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.http.header.HeaderGenerator;
import com.rainbowforest.productcatalogservice.service.ProductService;
import com.rainbowforest.productcatalogservice.service.AuditLogClient; // 🔥 IMPORT BƯU TÁ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired(required = false) // 🔥 CẤY BƯU TÁ
    private AuditLogClient auditLogClient;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProduct();
        return new ResponseEntity<>(products, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ? new ResponseEntity<>(product, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        productService.addProduct(product);

        // 🚀 BẮN LOG: THÊM SẢN PHẨM (Dùng tên thật)
        if (auditLogClient != null) {
            auditLogClient.sendLog(userName, "CREATE", "PRODUCT-SERVICE",
                    "Đã thêm sản phẩm mới: " + product.getProductName());
        }

        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product details,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        Product p = productService.getProductById(id);
        if (p != null) {
            p.setProductName(details.getProductName());
            p.setPrice(details.getPrice());
            p.setAvailability(details.getAvailability());
            p.setCategory(details.getCategory());
            p.setImageUrl(details.getImageUrl());

            productService.addProduct(p);

            // 🚀 BẮN LOG: SỬA SẢN PHẨM (Dùng tên thật)
            if (auditLogClient != null) {
                auditLogClient.sendLog(userName, "UPDATE", "PRODUCT-SERVICE",
                        "Cập nhật thông tin sản phẩm: " + p.getProductName());
            }

            return new ResponseEntity<>(p, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Name", defaultValue = "SYSTEM") String userName) { // 🔥 ĐÓN THẺ TÊN TỪ REACT

        productService.deleteProduct(id);

        // 🚀 BẮN LOG: XÓA SẢN PHẨM (Dùng tên thật)
        if (auditLogClient != null) {
            auditLogClient.sendLog(userName, "DELETE", "PRODUCT-SERVICE", "Đã xóa sản phẩm có ID: " + id);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}