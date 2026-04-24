package com.rainbowforest.contact_service.controller;

import com.rainbowforest.contact_service.entity.Contact;
import com.rainbowforest.contact_service.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    // Khách hàng gửi lời nhắn (React Client gọi)
    @PostMapping
    public ResponseEntity<Contact> sendContact(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactRepository.save(contact));
    }

    // Admin lấy danh sách lời nhắn (React Admin gọi)
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactRepository.findAllByOrderByCreatedAtDesc());
    }

    // Admin xóa lời nhắn (React Admin gọi)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}