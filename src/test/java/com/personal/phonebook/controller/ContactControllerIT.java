package com.personal.phonebook.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/contacts"})  //, "spring.main.allow-bean-definition-overriding=true"
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

        contactRepository.save(new Contact("Alice", "Smith", "123456789", "Tel Aviv"));
        contactRepository.save(new Contact("Bob", "Johnson", "987654321", "Haifa"));
        contactRepository.save(new Contact("Charlie", "Brown", "555666777", "Jerusalem"));
    }

    @Test
    public void testGetAllContactsWithoutQuery () {
        ResponseEntity<Contact[]> response = restTemplate.getForEntity(baseUrl, Contact[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(3);
    }

    @Test
    public void testSearchContactsByQueryWithPaging () {
        String urlWithQuery = baseUrl + "?query=ali&page=0&size=10";
        ResponseEntity<Contact[]> response = restTemplate.getForEntity(urlWithQuery, Contact[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);
        assertThat(response.getBody()[0].getFirstName()).isEqualTo("Alice");
    }

    @Test
    public void testSearchContactsByQueryWithPagingWithMultiple () {
        IntStream.range(0, 100).forEach(i -> contactRepository.save(new Contact("Smith", "Ali" + i, String.valueOf(i), "Tel Aviv")));

        String urlWithQuery = baseUrl + "?query=ali&page=0&size=101";
        ResponseEntity<Contact[]> response = restTemplate.getForEntity(urlWithQuery, Contact[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Contact[] body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.length).isEqualTo(101);
    }
}
