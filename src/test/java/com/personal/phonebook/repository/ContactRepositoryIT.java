package com.personal.phonebook.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.personal.phonebook.model.Contact;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.data.mongodb.uri=mongodb://localhost:27017/contacts")
public class ContactRepositoryIT {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup () {
        contactRepository.save(new Contact("John", "Doe", "123-456-7890", "123 Main St"));
        contactRepository.save(new Contact("Jane", "Smith", "456-789-0123", "456 Oak Ave"));
        contactRepository.save(new Contact("Bob", "Johnson", "789-012-3456", "789 Pine Rd"));
        contactRepository.save(new Contact("Alice", "Williams", "012-345-6789", "012 Elm Blvd"));
        contactRepository.save(new Contact("Charlie", "Brown", "345-678-9012", "345 Maple Ln"));
    }

    @AfterEach
    public void cleanup () {
        contactRepository.deleteAll();
    }

    @Test
    public void findAll_ReturnsAllContacts () {
        // When
        Page<Contact> result = contactRepository.findAll(PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    public void searchContacts_WithFirstNameQuery_ReturnsMatchingContacts () {
        // When
        Page<Contact> result = contactRepository.searchContacts("Jo", PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Contact::getFirstName).containsExactlyInAnyOrder("John", "Bob"); // Bob Jhonson
    }

    @Test
    public void searchContacts_WithLastNameQuery_ReturnsMatchingContacts () {
        // When
        Page<Contact> result = contactRepository.searchContacts("Sm", PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    public void searchContacts_WithPhoneQuery_ReturnsMatchingContacts () {
        // When
        Page<Contact> result = contactRepository.searchContacts("123-456", PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPhone()).isEqualTo("123-456-7890");
    }

    @Test
    public void searchContacts_WithAddressQuery_ReturnsMatchingContacts () {
        // When
        Page<Contact> result = contactRepository.searchContacts("ak", PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAddress()).isEqualTo("456 Oak Ave");
    }

    @Test
    public void searchContacts_WithNoMatches_ReturnsEmptyPage () {
        // When
        Page<Contact> result = contactRepository.searchContacts("NonexistentText", PageRequest.of(0, 10, Sort.by("firstName")));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void searchContacts_WithPagination_ReturnsPaginatedResults () {
        // When - requesting page 0 with size 2
        Page<Contact> page0 = contactRepository.findAll(PageRequest.of(0, 2, Sort.by("firstName")));

        // Then
        assertThat(page0).isNotNull();
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getTotalPages()).isEqualTo(3);

        // When - requesting page 1 with size 2
        Page<Contact> page1 = contactRepository.findAll(PageRequest.of(1, 2, Sort.by("firstName")));

        // Then
        assertThat(page1).isNotNull();
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getNumber()).isEqualTo(1);
    }
}