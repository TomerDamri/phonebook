package com.personal.phonebook.exception;

public class PhonebookException extends RuntimeException {
    public PhonebookException (String message) {
        super(message);
    }

    public PhonebookException (String message, Throwable cause) {
        super(message, cause);
    }
}