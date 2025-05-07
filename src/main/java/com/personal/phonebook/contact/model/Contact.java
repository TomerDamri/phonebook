package com.personal.phonebook.contact.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "contacts")
public class Contact {
    @Id
    private String id;

    @TextIndexed
    private String firstName;

    @TextIndexed
    private String lastName;

    @TextIndexed
    private String phone;

    @TextIndexed
    private String address;

    public Contact (String firstName, String lastName, String phone, String address) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
    }

    public Contact () {
    }
}