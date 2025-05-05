package com.personal.phonebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.service.ContactService;

@RestController
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contacts")
    public ResponseEntity<ContactsResponse> getContacts (@RequestParam(name = "query", required = false) String query,
                                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok().body(contactService.searchContacts(query, page, size));
    }
}
