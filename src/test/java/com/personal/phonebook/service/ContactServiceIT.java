package com.personal.phonebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.personal.phonebook.BaseIntegrationTest;
import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.model.Contact;

public class ContactServiceIT extends BaseIntegrationTest {

    @Test
    public void searchContacts_WithEmptyQuery_ReturnsAllContacts () {
        // When
        ContactsResponse result = contactService.searchContacts("", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(5);
        assertThat(result.getTotalCount()).isEqualTo(5);
    }

    @Test
    public void searchContacts_WithNullQuery_ReturnsAllContacts () {
        // When
        ContactsResponse result = contactService.searchContacts(null, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(5);
        assertThat(result.getTotalCount()).isEqualTo(5);
    }

    @Test
    public void searchContacts_WithFirstNameQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = contactService.searchContacts("Alice", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    public void searchContacts_WithPartialNameQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = contactService.searchContacts("Jo", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(2); // Should match "John" and "Johnson"
        assertThat(result.getContacts()).extracting(Contact::getLastName).containsAnyOf("Doe", "Johnson");
    }

    @Test
    public void searchContacts_WithPhoneQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = contactService.searchContacts("123-456", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getPhone()).isEqualTo("123-456-7890");
    }

    @Test
    public void searchContacts_WithAddressQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = contactService.searchContacts("Oak", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getAddress()).isEqualTo("456 Oak Ave");
    }

    @Test
    public void searchContacts_WithNoMatches_ReturnsEmptyResults () {
        // When
        ContactsResponse result = contactService.searchContacts("NonexistentText", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).isEmpty();
        assertThat(result.getTotalCount()).isEqualTo(0);
    }

    @Test
    public void searchContacts_WithTooLargePageSize_ThrowsException () {
        // Given
        int pageSize = 100; // Larger than maxPageSize=10

        // When + Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> contactService.searchContacts("query", 0, pageSize));

        assertThat(exception.getMessage()).contains("Page size cannot be larger than 10");
    }

    @Test
    public void searchContacts_WithPagination_ReturnsPaginatedResults () {
        // When - requesting page 0 with size 2
        ContactsResponse page0 = contactService.searchContacts(null, 0, 2);

        // Then
        assertThat(page0).isNotNull();
        assertThat(page0.getContacts()).hasSize(2);
        assertThat(page0.getTotalCount()).isEqualTo(5);

        // When - requesting page 1 with size 2
        ContactsResponse page1 = contactService.searchContacts(null, 1, 2);

        // Then
        assertThat(page1).isNotNull();
        assertThat(page1.getContacts()).hasSize(2);

        // When - requesting page 2 with size 2 (should have only 1 contact)
        ContactsResponse page2 = contactService.searchContacts(null, 2, 2);

        // Then
        assertThat(page2).isNotNull();
        assertThat(page2.getContacts()).hasSize(1);
    }

    @Test
    public void createContact_ReturnsCreatedContact () {
        // Given
        Contact newContact = new Contact("New", "User", "111-222-3333", "999 New St");

        // When
        Contact result = contactService.createContact(newContact);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhone()).isEqualTo("111-222-3333");
        assertThat(result.getAddress()).isEqualTo("999 New St");

        // Verify it's in the database
        assertThat(contactRepository.findById(result.getId())).isPresent();
    }

    @Test
    public void updateContact_WithExistingId_ReturnsUpdatedContact () {
        // Given
        Contact updatedDetails = new Contact("John", "Updated", "999-999-9999", "999 Updated Ave");

        // When
        Contact result = contactService.updateContact(testContactId, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testContactId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Updated");
        assertThat(result.getPhone()).isEqualTo("999-999-9999");
        assertThat(result.getAddress()).isEqualTo("999 Updated Ave");

        // Verify it's updated in the database
        Contact fromDb = contactRepository.findById(testContactId).orElseThrow();
        assertThat(fromDb.getLastName()).isEqualTo("Updated");
        assertThat(fromDb.getPhone()).isEqualTo("999-999-9999");
        assertThat(fromDb.getAddress()).isEqualTo("999 Updated Ave");
    }

    @Test
    public void updateContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        String nonExistingId = "non-existing-id";
        Contact updatedDetails = new Contact("John", "Updated", "999-999-9999", "999 Updated Ave");

        // When + Then
        assertThrows(ContanctNotFoundException.class, () -> contactService.updateContact(nonExistingId, updatedDetails));
    }

    @Test
    public void deleteContact_WithExistingId_DeletesContact () {
        // Given
        assertThat(contactRepository.existsById(testContactId)).isTrue();

        // When
        contactService.deleteContact(testContactId);

        // Then
        assertThat(contactRepository.existsById(testContactId)).isFalse();
    }

    @Test
    public void deleteContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        String nonExistingId = "non-existing-id";

        // When + Then
        assertThrows(ContanctNotFoundException.class, () -> contactService.deleteContact(nonExistingId));
    }
}