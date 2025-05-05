package com.personal.phonebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@Service
public class ContactService {

    private static final String FIRST_NAME = "firstName";
    private static final Sort SORT = Sort.by(Sort.Direction.ASC, FIRST_NAME);
    @Value("${phonebook.pagination.max-page-size}")
    private int maxPageSize;

    @Autowired
    private ContactRepository contactRepository;

    public ContactsResponse searchContacts (String query, int page, int size) {
        validatePageSize(size);
        if (query == null || query.isEmpty()) {
            return getContacts(page, size);
        }
        Page<Contact> result = contactRepository.searchContacts(query, PageRequest.of(page, size, SORT));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }

    private void validatePageSize (int size) {
        if (size > maxPageSize) {
            throw new IllegalArgumentException("Page size cannot be larger than " + maxPageSize);
        }
    }

    private ContactsResponse getContacts (int page, int size) {
        Page<Contact> result = contactRepository.findAll(PageRequest.of(page, size, SORT));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }
}