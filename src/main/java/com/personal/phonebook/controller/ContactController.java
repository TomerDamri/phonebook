package com.personal.phonebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.service.ContactService;

@RestController
@RequestMapping("/phonebook")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<Contact> createContact (@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createContact(contact));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact (@PathVariable String id, @RequestBody Contact contact) {
        return ResponseEntity.ok().body(contactService.updateContact(id, contact));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact (@PathVariable String id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/contacts")
    public ResponseEntity<ContactsResponse> getContacts (@RequestParam(name = "query", required = false) String query,
                                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                                         @RequestParam(name = "size", defaultValue = "10") int size,
                                                         @RequestParam(name = "direction", defaultValue = "ASC") String direction,
                                                         @RequestParam(name = "sortBy", defaultValue = "firstName") String sortBy) {
        return ResponseEntity.ok().body(contactService.searchContacts(query, page, size, direction, sortBy));
    }
}
