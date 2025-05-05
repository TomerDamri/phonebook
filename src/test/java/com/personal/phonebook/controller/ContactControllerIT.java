package com.personal.phonebook.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.data.mongodb.uri=mongodb://localhost:27017/contacts")
public class ContactControllerIT {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContactRepository contactRepository;

    private String baseUrl;

    @BeforeEach
    public void setup () {
        baseUrl = "http://localhost:" + port + "/contacts";
        contactRepository.deleteAll();
    }

    @Test
    public void testGetAllContactsWithoutQuery () {
        // Arrange
        prepareSmallDataForTest();

        // Act
        ResponseEntity<ContactsResponse> response = getContacts();

        // Assert
        ContactsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getContacts().size()).isEqualTo(3);
    }

    @Test
    public void testSearchContactsByQueryWithPaging () {
        // Arrange
        prepareSmallDataForTest();

        // Act
        ResponseEntity<ContactsResponse> response = searchContacts("ali", 0, 10);

        // Assert
        assertSearchResponse(response, 1);
        assertContact(response.getBody().getContacts().get(0), "Alice");
    }

    @Test
    public void testSearchContactsByQueryWithPagingWithMultipleAnswers () {
        // Arrange
        prepareLargeDataForTests();
        int size = 10;

        // Act & Assert
        validatePaginatedResults(size);
    }

    private void validatePaginatedResults (int size) {
        int totalContacts = 0;
        int page = 0;
        char expectedLetter = 'a';

        while (true) {
            ResponseEntity<ContactsResponse> response = searchContacts("ali", page, size);
            assertSearchResponse(response, 26);

            List<Contact> pageContacts = response.getBody().getContacts();
            if (pageContacts.isEmpty()) {
                break;
            }

            expectedLetter = validatePageResults(pageContacts, expectedLetter);
            totalContacts += pageContacts.size();
            page++;
        }

        assertThat(totalContacts).isEqualTo(26);
        assertThat(expectedLetter).isEqualTo('{'); // Verify we've seen all letters
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