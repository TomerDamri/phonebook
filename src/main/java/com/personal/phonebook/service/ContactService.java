package com.personal.phonebook.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public List<Contact> searchContacts (String query, int page, int size) {
        if (query == null || query.isEmpty()) {
            return getContacts(page, size);
        }
        Page<Contact> result = contactRepository.searchContacts(query, PageRequest.of(page, size));
        return result.getContent();
    }

    public List<Contact> getContacts (int page, int size) {
        Page<Contact> result = contactRepository.findAll(PageRequest.of(page, size));
        return result.getContent();
    }
}