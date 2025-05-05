package com.personal.phonebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class PhonebookApplication {

    public static void main (String[] args) {
        log.info("Starting Phonebook API application");
        SpringApplication.run(PhonebookApplication.class, args);
        log.info("Phonebook API application started successfully");
    }

}
