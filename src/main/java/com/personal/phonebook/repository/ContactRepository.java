package com.personal.phonebook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.personal.phonebook.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("SELECT c FROM Contact c WHERE " + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                + "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " + "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                + "LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Contact> searchContacts (String query, Pageable pageable);
}
