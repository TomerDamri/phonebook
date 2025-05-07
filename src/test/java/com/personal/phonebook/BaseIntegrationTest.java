package com.personal.phonebook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.personal.phonebook.contact.model.Contact;
import com.personal.phonebook.contact.repository.ContactRepository;
import com.personal.phonebook.contact.service.ContactService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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

    @Autowired
    protected MeterRegistry meterRegistry;

    protected String testContactId;

    @BeforeEach
    protected void setup () {
        createTestData();
    }

    @AfterEach
    public void cleanup () {
        contactRepository.deleteAll();
        resetMetrics();
    }

    private void resetMetrics () {
        // Reset operation counters
        resetCounter("phonebook.contacts.search");
        resetCounter("phonebook.contacts.created");
        resetCounter("phonebook.contacts.updated");
        resetCounter("phonebook.contacts.deleted");

        // Reset timers
        resetTimer("search_contacts_timer");
        resetTimer("create_contact_timer");
        resetTimer("update_contact_timer");
        resetTimer("delete_contact_timer");
    }

    private void resetCounter (String name) {
        Counter counter = meterRegistry.find(name).counter();
        if (counter != null) {
            meterRegistry.remove(counter);
            meterRegistry.counter(name);
        }
    }

    private void resetTimer (String name) {
        Timer timer = meterRegistry.find(name).timer();
        if (timer != null) {
            meterRegistry.remove(timer);
            meterRegistry.timer(name);
        }
    }

    private void createTestData () {
        testContactId = contactRepository.save(new Contact("John", "Doe", "123-456-7890", "123 Main St")).getId();
        contactRepository.save(new Contact("Jane", "Smith", "456-789-0123", "456 Oak Ave"));
        contactRepository.save(new Contact("Bob", "Johnson", "789-012-3456", "789 Pine Rd"));
        contactRepository.save(new Contact("Alice", "Williams", "012-345-6789", "012 Elm Blvd"));
        contactRepository.save(new Contact("Charlie", "Brown", "345-678-9012", "345 Maple Ln"));
    }
}
