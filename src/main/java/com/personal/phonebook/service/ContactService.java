package com.personal.phonebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public ContactsResponse searchContacts (String query, int page, int size) {
        if (query == null || query.isEmpty()) {
            return getContacts(page, size);
        }
        Page<Contact> result = contactRepository.searchContacts(query,
                                                                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName")));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }

    private ContactsResponse getContacts (int page, int size) {
        Page<Contact> result = contactRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName")));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }
}