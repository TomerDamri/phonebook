package com.personal.phonebook.exception.controller;

import java.time.LocalDateTime;

import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.exception.NotFoundException;
import com.personal.phonebook.exception.PhonebookException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.personal.phonebook.exception.controller.response.ErrorResponse;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({ IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest (IllegalArgumentException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                ex.getMessage(),
                                                request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ContanctNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound (NotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(),
                                                HttpStatus.NOT_FOUND.value(),
                                                "Not Found",
                                                ex.getMessage(),
                                                request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PhonebookException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError (PhonebookException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal Server Error",
                                                ex.getMessage(),
                                                request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}