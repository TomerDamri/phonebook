package com.personal.phonebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.personal.phonebook.BaseIntegrationTest;
import com.personal.phonebook.contact.controller.response.ContactsResponse;
import com.personal.phonebook.contact.model.Contact;
import com.personal.phonebook.exception.ContanctNotFoundException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

public class ContactServiceIT extends BaseIntegrationTest {

    @Test
    public void searchContacts_WithEmptyQuery_ReturnsAllContacts () {
        // When
        ContactsResponse result = searchContacts("", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(5);
        assertThat(result.getTotalCount()).isEqualTo(5);
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithNullQuery_ReturnsAllContacts () {
        // When
        ContactsResponse result = searchContacts(null, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(5);
        assertThat(result.getTotalCount()).isEqualTo(5);
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithFirstNameQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = searchContacts("Alice", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getFirstName()).isEqualTo("Alice");
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithPartialNameQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = searchContacts("Jo", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(2); // Should match "John" and "Johnson"
        assertThat(result.getContacts()).extracting(Contact::getLastName).containsAnyOf("Doe", "Johnson");
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithPhoneQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = searchContacts("123-456", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getPhone()).isEqualTo("123-456-7890");
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithAddressQuery_ReturnsMatchingContacts () {
        // When
        ContactsResponse result = searchContacts("Oak", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).hasSize(1);
        assertThat(result.getContacts().get(0).getAddress()).isEqualTo("456 Oak Ave");
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithNoMatches_ReturnsEmptyResults () {
        // When
        ContactsResponse result = searchContacts("NonexistentText", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContacts()).isEmpty();
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithTooLargePageSize_ThrowsException () {
        // Given
        int pageSize = 100; // Larger than maxPageSize=10

        // When + Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> searchContacts("query", 0, pageSize));

        assertThat(exception.getMessage()).contains("Page size cannot be larger than 10");
//        assertMetricNotIncremented("phonebook.contacts.search");
        assertTimerMetricNotRecorded("search_contacts_timer");
    }

    @Test
    public void searchContacts_WithPagination_ReturnsPaginatedResults () {
        // When - requesting page 0 with size 2
        ContactsResponse page0 = searchContacts(null, 0, 2);

        // Then
        assertThat(page0).isNotNull();
        assertThat(page0.getContacts()).hasSize(2);
        assertThat(page0.getTotalCount()).isEqualTo(5);

        // When - requesting page 1 with size 2
        ContactsResponse page1 = searchContacts(null, 1, 2);

        // Then
        assertThat(page1).isNotNull();
        assertThat(page1.getContacts()).hasSize(2);

        // When - requesting page 2 with size 2 (should have only 1 contact)
        ContactsResponse page2 = searchContacts(null, 2, 2);

        // Then
        assertThat(page2).isNotNull();
        assertThat(page2.getContacts()).hasSize(1);
        assertMetricIncremented("phonebook.contacts.search");
        assertTimerMetricRecorded("search_contacts_timer");
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
        assertThat(contactRepository.findById(result.getId())).isPresent();
        assertMetricIncremented("phonebook.contacts.created");
        assertTimerMetricRecorded("create_contact_timer");
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
        Contact fromDb = contactRepository.findById(testContactId).orElseThrow();
        assertThat(fromDb.getLastName()).isEqualTo("Updated");
        assertThat(fromDb.getPhone()).isEqualTo("999-999-9999");
        assertThat(fromDb.getAddress()).isEqualTo("999 Updated Ave");
        assertMetricIncremented("phonebook.contacts.updated");
        assertTimerMetricRecorded("update_contact_timer");
    }

    @Test
    public void updateContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        String nonExistingId = "non-existing-id";
        Contact updatedDetails = new Contact("John", "Updated", "999-999-9999", "999 Updated Ave");

        // When + Then
        assertThrows(ContanctNotFoundException.class, () -> contactService.updateContact(nonExistingId, updatedDetails));
//        assertMetricNotIncremented("phonebook.contacts.updated");
        assertTimerMetricNotRecorded("update_contact_timer");
    }

    @Test
    public void deleteContact_WithExistingId_DeletesContact () {
        // Given
        assertThat(contactRepository.existsById(testContactId)).isTrue();

        // When
        contactService.deleteContact(testContactId);

        // Then
        assertThat(contactRepository.existsById(testContactId)).isFalse();
        assertMetricIncremented("phonebook.contacts.deleted");
        assertTimerMetricRecorded("delete_contact_timer");
    }

    @Test
    public void deleteContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        String nonExistingId = "non-existing-id";

        // When + Then
        assertThrows(ContanctNotFoundException.class, () -> contactService.deleteContact(nonExistingId));
//        assertMetricNotIncremented("phonebook.contacts.deleted");
        assertTimerMetricNotRecorded("delete_contact_timer");
    }

    private ContactsResponse searchContacts (String query, int page, int size, String direction, String sortBy) {
        return contactService.searchContacts(query, page, size, direction, sortBy);
    }

    private ContactsResponse searchContacts (String query, int page, int size) {
        return searchContacts(query, page, size, "ASC", "firstName");
    }

    private void assertMetricIncremented (String metricName) {
        double count = meterRegistry.get(metricName).counter().count();
        assertThat(count).isPositive();
    }

    private void assertTimerMetricRecorded (String timerName) {
        Timer timer = meterRegistry.get(timerName).timer();
        assertThat(timer.count()).isPositive();
    }

//    private void assertMetricNotIncremented (String metricName) {
//        Counter counter = meterRegistry.get(metricName).counter();
//        counter.count()
//        // assertThat(count).isZero();
//    }

    private void assertTimerMetricNotRecorded (String timerName) {
        Timer timer = meterRegistry.get(timerName).timer();
        assertThat(timer.count()).isZero();
    }

}