package com.personal.phonebook.contact.controller.response;

import java.util.List;

import com.personal.phonebook.contact.model.Contact;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactsResponse {
    private List<Contact> contacts;
    private long totalCount;
}