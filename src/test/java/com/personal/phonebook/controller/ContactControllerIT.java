package com.personal.phonebook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.personal.phonebook.BaseIntegrationTest;
import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.model.Contact;

public class ContactControllerIT extends BaseIntegrationTest {
    private String baseUrl;

    @Override
    @BeforeEach
    public void setup () {
        baseUrl = "http://localhost:" + port + "/contacts";
        contactRepository.deleteAll();
    }

    @Test
    public void getContacts_WithInvalidPageSize_ReturnsBadRequest () {
        // Given
        prepareSmallDataForTest();
        String urlWithInvalidSize = String.format("%s?size=%d", baseUrl, 11);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(urlWithInvalidSize, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Page size cannot be larger than 10");
    }

    @Test
    public void getContacts_WithoutQuery_ReturnsAllContacts () {
        // Given
        prepareSmallDataForTest();

        // When
        ResponseEntity<ContactsResponse> response = getContacts();

        // Then
        ContactsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getContacts().size()).isEqualTo(3);
    }

    @Test
    public void searchContacts_WithQuery_ReturnsPaginatedResult () {
        // Given
        prepareSmallDataForTest();

        // When
        ResponseEntity<ContactsResponse> response = searchContacts("ali", 0, 10);

        // Then
        assertSearchResponse(response, 1);
        assertContact(response.getBody().getContacts().get(0), "Alice");
    }

    @Test
    public void searchContacts_WithQuery_ReturnsPaginatedResultsWithMultiplePages () {
        // Given
        prepareLargeDataForTests();
        int size = 10;
        // When & Then
        validatePaginatedResults(size);
    }

    @Test
    void searchContacts_AcrossAllFields_ReturnsMatchingContacts () {
        // Given
        List<Contact> testContacts = List.of(new Contact("Bobby", "Smith", "123-456", "Main St"), // matches firstName
                                             new Contact("John", "Bibby", "123-456", "Oak St"), // matches lastName
                                             new Contact("Alice", "Jones", "123-456", "Bobbit Ave"), // matches address
                                             new Contact("Mike", "Wilson", "123-456", "Pine St"), // no match
                                             new Contact("Sarah", "Davis", "123-456", "Elm St"), // no match
                                             new Contact("Tom", "Brown", "123-456", "Cedar St"), // no match
                                             new Contact("Emma", "Taylor", "123-456", "Maple St"), // no match
                                             new Contact("James", "Miller", "123-456", "Birch St"), // no match
                                             new Contact("Lucy", "Moore", "123-456", "Spruce St"), // no match
                                             new Contact("David", "Clark", "123-456", "Willow St") // no match
        );
        contactRepository.saveAll(testContacts);
        // When
        ResponseEntity<ContactsResponse> response = searchContacts("bb", 0, 10);
        // Then
        assertSearchResponse(response, 3);
        List<Contact> actualContacts = response.getBody().getContacts();

        assertEquals(3, actualContacts.size());
        Assertions.assertTrue(actualContacts.get(0).getAddress().contains("Bobbit"));
        assertEquals("Bobby", actualContacts.get(1).getFirstName());
        assertEquals("Bibby", actualContacts.get(2).getLastName());
    }

    private void validatePaginatedResults (int size) {
        int totalContactsForSearchTerm = 0;
        int totalContactsForAll = 0;
        int page = 0;
        char expectedLetterForSearchTerm = 'a';
        char expectedLetterForAll = 'a';
        while (true) {
            ResponseEntity<ContactsResponse> response = searchContacts("ali", page, size);
            assertSearchResponse(response, 26);

            List<Contact> pageContacts = response.getBody().getContacts();
            if (pageContacts.isEmpty()) {
                break;
            }

            expectedLetterForSearchTerm = validatePageResults(pageContacts, expectedLetterForSearchTerm);

            // verify pagination when no searchTerm is provided
            ResponseEntity<ContactsResponse> responseForAll = getContacts(page, size);
            // Then
            assertSearchResponse(responseForAll, 26);
            ContactsResponse body = responseForAll.getBody();
            assertThat(body).isNotNull();
            expectedLetterForAll = validatePageResults(body.getContacts(), expectedLetterForAll);

            totalContactsForSearchTerm += pageContacts.size();
            totalContactsForAll += body.getContacts().size();
            page++;
        }

        assertThat(totalContactsForSearchTerm).isEqualTo(26);
        assertThat(totalContactsForAll).isEqualTo(26);
        assertThat(expectedLetterForSearchTerm).isEqualTo('{');
        assertThat(expectedLetterForAll).isEqualTo('{');// Verify we've seen all letters
    }

    private char validatePageResults (List<Contact> pageContacts, char expectedLetter) {
        for (Contact contact : pageContacts) {
            String expectedName = "Alice" + expectedLetter;
            assertThat(contact.getFirstName()).isEqualTo(expectedName);
            expectedLetter++;
        }
        return expectedLetter;
    }

    private ResponseEntity<ContactsResponse> getContacts () {
        return restTemplate.getForEntity(baseUrl, ContactsResponse.class);
    }

    private ResponseEntity<ContactsResponse> getContacts (int page, int size) {
        String urlWithQuery = String.format("%s?page=%d&size=%d", baseUrl, page, size);
        return restTemplate.getForEntity(urlWithQuery, ContactsResponse.class);
    }

    private ResponseEntity<ContactsResponse> searchContacts (String query, int page, int size) {
        String urlWithQuery = String.format("%s?query=%s&page=%d&size=%d", baseUrl, query, page, size);
        return restTemplate.getForEntity(urlWithQuery, ContactsResponse.class);
    }

    private void assertSearchResponse (ResponseEntity<ContactsResponse> response, int expectedTotalCount) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalCount()).isEqualTo(expectedTotalCount);
    }

    private void assertContact (Contact contact, String expectedFirstName) {
        assertThat(contact.getFirstName()).isEqualTo(expectedFirstName);
    }

    private void prepareLargeDataForTests () {
        for (char letter = 'z'; letter >= 'a'; letter--) {
            Contact contact = new Contact("Alice" + letter, "Smith", String.valueOf((int) letter), "Tel Aviv");
            contactRepository.save(contact);
        }
    }

    private void prepareSmallDataForTest () {
        contactRepository.save(new Contact("Alice", "Smith", "123456789", "Tel Aviv"));
        contactRepository.save(new Contact("Bob", "Johnson", "987654321", "Haifa"));
        contactRepository.save(new Contact("Charlie", "Brown", "555666777", "Jerusalem"));
    }
}