package com.personal.phonebook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

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

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

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
        when(contactRepository.searchContacts(eq("John"), any(PageRequest.class))).thenReturn(contactPage);
        // When
        ContactsResponse response = contactService.searchContacts("John", 0, 5, "ASC", "firstName");
        // Then
        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        assertEquals("John", response.getContacts().get(0).getFirstName());
        verify(contactRepository).searchContacts(eq("John"), any(PageRequest.class));
    }

    @Test
    public void searchContacts_WithoutQuery_ReturnsAllContacts () {
        // Given
        when(contactRepository.findAll(any(PageRequest.class))).thenReturn(contactPage);
        // When
        ContactsResponse response = contactService.searchContacts(null, 0, 5, "ASC", "firstName");
        // Then
        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        verify(contactRepository).findAll(any(PageRequest.class));
    }

    @Test
    public void searchContacts_WithInvalidPageSize_ThrowsException () {
        // When + Then
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () -> {
            contactService.searchContacts(null, 0, 20, "ASC", "firstName");
        });
        assertTrue(actualException.getMessage().contains("Page size cannot be larger than 10"));
    }

    @Test
    public void createContact_ReturnsCreatedContact () {
        // Given
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);
        // When
        Contact result = contactService.createContact(testContact);
        // Then
        assertEquals("John", result.getFirstName());
        assertEquals("test-id-123", result.getId());
    }

    @Test
    public void updateContact_WithExistingId_ReturnsUpdatedContact () {
        // Given
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
    }

    @Test
    public void updateContact_WithNonExistingId_ThrowsNotFoundException () {
        // When
        when(contactRepository.findById("nonexistent-id")).thenReturn(Optional.empty());
        // Then
        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.updateContact("nonexistent-id", testContact));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
    }

    @Test
    public void deleteContact_WithExistingId_DeletesContact () {
        // Given
        when(contactRepository.existsById("test-id-123")).thenReturn(true);
        doNothing().when(contactRepository).deleteById("test-id-123");
        // When
        contactService.deleteContact("test-id-123");
        // Then
        verify(contactRepository).deleteById("test-id-123");
    }

    @Test
    public void deleteContact_WithNonExistingId_ThrowsNotFoundException () {
        // Given
        when(contactRepository.existsById("nonexistent-id")).thenReturn(false);
        // When + Then
        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.deleteContact("nonexistent-id"));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
        verify(contactRepository, never()).deleteById(anyString());
    }
}