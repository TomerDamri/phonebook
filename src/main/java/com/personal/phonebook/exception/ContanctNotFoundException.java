package com.personal.phonebook.exception;

import com.personal.phonebook.contact.model.Contact;

public class ContanctNotFoundException extends NotFoundException {
    public ContanctNotFoundException (String articleId) {
        super(Contact.class, articleId);
    }
}