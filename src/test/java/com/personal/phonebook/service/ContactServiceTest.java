package com.personal.phonebook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.personal.phonebook.contact.controller.response.ContactsResponse;
import com.personal.phonebook.contact.model.Contact;
import com.personal.phonebook.contact.repository.ContactRepository;
import com.personal.phonebook.contact.service.ContactService;
import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.metric.ContactMetricsImpl;

import io.micrometer.core.instrument.Timer;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactMetricsImpl contactMetricsImpl;

    @Mock
    private Timer timer;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private Page<Contact> contactPage;

    @BeforeEach
    public void setUp () {
        testContact = new Contact("John", "Doe", "123-456-7890", "123 Main St");
        testContact.setId("test-id-123");
        contactPage = new PageImpl<>(List.of(testContact));
        ReflectionTestUtils.setField(contactService, "maxPageSize", 10);
    }

    @Test
    public void searchContacts_WithQuery_ReturnsMatchingContacts () {
        // Given
        setupContactMetricsMock();
        when(contactRepository.searchContacts(eq("John"), any(PageRequest.class))).thenReturn(contactPage);
        // When
        ContactsResponse response = contactService.searchContacts("John", 0, 5, "ASC", "firstName");
        // Then
        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        assertEquals("John", response.getContacts().get(0).getFirstName());
        verify(contactRepository).searchContacts(eq("John"), any(PageRequest.class));
        verify(contactMetricsImpl).incrementContactSearch();
    }

    @Test
    public void searchContacts_WithoutQuery_ReturnsAllContacts () {
        // Given
        setupContactMetricsMock();
        when(contactRepository.findAll(any(PageRequest.class))).thenReturn(contactPage);
        // When
        ContactsResponse response = contactService.searchContacts(null, 0, 5, "ASC", "firstName");
        // Then
        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        verify(contactRepository).findAll(any(PageRequest.class));
        verify(contactMetricsImpl).incrementContactSearch();
    }

    @Test
    public void searchContacts_WithInvalidPageSize_ThrowsException () {
        // When + Then
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () -> {
            contactService.searchContacts(null, 0, 20, "ASC", "firstName");
        });
        assertTrue(actualException.getMessage().contains("Page size cannot be larger than 10"));
        verify(contactMetricsImpl, never()).incrementContactSearch();
    }

    @Test
    public void createContact_ReturnsCreatedContact () {
        // Given
        setupContactMetricsMock();
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
        // When
        Contact result = contactService.createContact(testContact);
        // Then
        assertEquals("John", result.getFirstName());
        assertEquals("test-id-123", result.getId());
        verify(contactMetricsImpl).incrementContactCreated();
    }

    @Test
    public void updateContact_WithExistingId_ReturnsUpdatedContact () {
        // Given
        setupContactMetricsMock();
        Contact updatedContact = new Contact("Jane", "Doe", "987-654-3210", "456 New St");
        when(contactRepository.findById("test-id-123")).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            assertEquals("Jane", savedContact.getFirstName());
            assertEquals("test-id-123", savedContact.getId());
            return savedContact;
        });
        // When
        Contact result = contactService.updateContact("test-id-123", updatedContact);
        // Then
        assertEquals("Jane", result.getFirstName());
        verify(contactMetricsImpl).incrementContactUpdated();
    }

    @Test
    public void updateContact_WithNonExistingId_ThrowsNotFoundException () {
        // When
        when(contactRepository.findById("nonexistent-id")).thenReturn(Optional.empty());
        // Then
        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.updateContact("nonexistent-id", testContact));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
        verify(contactMetricsImpl, never()).incrementContactUpdated();
    }

    @Test
    public void deleteContact_WithExistingId_DeletesContact () {
        // Given
        mockGetContactOperationTimer();
        when(contactRepository.existsById("test-id-123")).thenReturn(true);
        // When
        contactService.deleteContact("test-id-123");
        // Then
        verify(contactMetricsImpl).incrementContactDeleted();
    }

    @Test
    public void deleteContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        when(contactRepository.existsById("nonexistent-id")).thenReturn(false);
        // When + Then
        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.deleteContact("nonexistent-id"));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
        verify(contactMetricsImpl, never()).incrementContactDeleted();
    }

    private void setupContactMetricsMock () {
        mockGetContactOperationTimer();
        mockTimer();
    }

    private void mockGetContactOperationTimer () {
        when(contactMetricsImpl.getContactOperationTimer()).thenReturn(timer);
    }

    private void mockTimer () {
        when(timer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }
}