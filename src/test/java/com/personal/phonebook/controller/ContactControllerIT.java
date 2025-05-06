package com.personal.phonebook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
        baseUrl = "http://localhost:" + port + "/phonebook";
        contactRepository.deleteAll();
    }

    @Test
    public void getContacts_WithInvalidPageSize_ReturnsBadRequest () {
        // Given
        prepareSmallDataForTest();
        String urlWithInvalidSize = String.format("%s/contacts?size=%d", baseUrl, 11);

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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    @Test
    public void createContact_WithValidData_ReturnsCreatedContact () {
        // Given
        Contact newContact = new Contact("Test", "User", "123-456-7890", "Test Address");
        // When
        ResponseEntity<Contact> response = restTemplate.postForEntity(baseUrl, newContact, Contact.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Test");

        // Verify it's in the database
        Contact saved = contactRepository.findById(response.getBody().getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Test");
    }

    @Test
    public void createContact_WithInvalidData_ReturnsBadRequest () {
        // Given
        Contact invalidContact = new Contact("", "", "", "");
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, invalidContact, String.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateContact_WithValidData_ReturnsUpdatedContact () {
        // Given
        Contact initial = contactRepository.save(new Contact("Initial", "User", "111-111-1111", "Initial Address"));
        Contact updated = new Contact("Updated", "User", "222-222-2222", "Updated Address");
        // When
        ResponseEntity<Contact> response = restTemplate.exchange(baseUrl + "/" + initial.getId(),
                                                                 HttpMethod.PUT,
                                                                 new HttpEntity<>(updated),
                                                                 Contact.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Updated");

        // Verify it's updated in the database
        Contact savedContact = contactRepository.findById(initial.getId()).orElse(null);
        assertThat(savedContact).isNotNull();
        assertThat(savedContact.getFirstName()).isEqualTo("Updated");
    }

    @Test
    public void updateContact_WithNonExistingId_ReturnsNotFound () {
        // Given
        Contact updated = new Contact("Updated", "User", "222-222-2222", "Updated Address");
        // When
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/non-existing-id",
                                                                HttpMethod.PUT,
                                                                new HttpEntity<>(updated),
                                                                String.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteContact_WithExistingId_ReturnsNoContent () {
        // Given
        Contact contact = contactRepository.save(new Contact("ToDelete", "User", "111-111-1111", "Delete Address"));
        // When
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/" + contact.getId(), HttpMethod.DELETE, null, Void.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(contactRepository.findById(contact.getId())).isEmpty();
    }

    @Test
    public void deleteContact_WithNonExistingId_ReturnsNotFound () {
        // When
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/non-existing-id", HttpMethod.DELETE, null, String.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
        return restTemplate.getForEntity(String.format("%s/contacts", baseUrl), ContactsResponse.class);
    }

    private ResponseEntity<ContactsResponse> getContacts (int page, int size) {
        String urlWithQuery = String.format("%s/contacts?page=%d&size=%d", baseUrl, page, size);
        return restTemplate.getForEntity(urlWithQuery, ContactsResponse.class);
    }

    private ResponseEntity<ContactsResponse> searchContacts (String query, int page, int size) {
        String urlWithQuery = String.format("%s/contacts?query=%s&page=%d&size=%d", baseUrl, query, page, size);
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