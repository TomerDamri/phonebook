package com.personal.phonebook.controller.response;

import java.util.List;

import com.personal.phonebook.model.Contact;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactsResponse {
    private List<Contact> contacts;
    private long totalCount;
}