package com.personal.phonebook.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.personal.phonebook.model.Contact;

public interface ContactRepository extends MongoRepository<Contact, String> {

    // Using regex for partial matching
    @Query("{ $or: [ " +
            "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'phone': { $regex: ?0, $options: 'i' } }, " +
            "{ 'address': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Contact> searchContacts(String text, Pageable pageable);
}
