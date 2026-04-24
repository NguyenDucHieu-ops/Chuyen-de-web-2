package com.rainbowforest.contact_service.repository;

import com.rainbowforest.contact_service.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Hàm này giúp xếp các tin nhắn mới nhất lên đầu tiên
    List<Contact> findAllByOrderByCreatedAtDesc();
}