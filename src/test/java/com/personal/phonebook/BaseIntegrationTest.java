package com.personal.phonebook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;
import com.personal.phonebook.service.ContactService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.data.mongodb.uri=mongodb://localhost:27017/contacts")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ContactRepository contactRepository;

    @Autowired
    protected ContactService contactService;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String testContactId;

    @BeforeEach
    protected void setup () {
        // Create test data
        testContactId = contactRepository.save(new Contact("John", "Doe", "123-456-7890", "123 Main St")).getId();
        contactRepository.save(new Contact("Jane", "Smith", "456-789-0123", "456 Oak Ave"));
        contactRepository.save(new Contact("Bob", "Johnson", "789-012-3456", "789 Pine Rd"));
        contactRepository.save(new Contact("Alice", "Williams", "012-345-6789", "012 Elm Blvd"));
        contactRepository.save(new Contact("Charlie", "Brown", "345-678-9012", "345 Maple Ln"));
    }

    @AfterEach
    protected void cleanup () {
        contactRepository.deleteAll();
    }
}
