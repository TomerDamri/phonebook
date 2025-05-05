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
    void setUp () {
        testContact = new Contact("John", "Doe", "123-456-7890", "123 Main St");
        testContact.setId("test-id-123");

        contactPage = new PageImpl<>(List.of(testContact));

        // Set maxPageSize using reflection
        ReflectionTestUtils.setField(contactService, "maxPageSize", 10);
    }

    @Test
    void searchContactsWithQueryTest () {
        when(contactRepository.searchContacts(eq("John"), any(PageRequest.class))).thenReturn(contactPage);

        ContactsResponse response = contactService.searchContacts("John", 0, 5);

        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        assertEquals("John", response.getContacts().get(0).getFirstName());
        verify(contactRepository).searchContacts(eq("John"), any(PageRequest.class));
    }

    @Test
    void searchContactsWithoutQueryTest () {
        when(contactRepository.findAll(any(PageRequest.class))).thenReturn(contactPage);

        ContactsResponse response = contactService.searchContacts(null, 0, 5);

        assertEquals(1, response.getContacts().size());
        assertEquals(1, response.getTotalCount());
        verify(contactRepository).findAll(any(PageRequest.class));
    }

    @Test
    void searchContactsWithInvalidPageSize () {
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () -> {
            contactService.searchContacts(null, 0, 20);
        });

        assertTrue(actualException.getMessage().contains("Page size cannot be larger than 10"));
    }

    @Test
    void createContactTest () {
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        Contact result = contactService.createContact(testContact);

        assertEquals("John", result.getFirstName());
        assertEquals("test-id-123", result.getId());
    }

    @Test
    void updateContactTest () {
        Contact updatedContact = new Contact("Jane", "Doe", "987-654-3210", "456 New St");

        when(contactRepository.findById("test-id-123")).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            assertEquals("Jane", savedContact.getFirstName());
            assertEquals("test-id-123", savedContact.getId());
            return savedContact;
        });

        Contact result = contactService.updateContact("test-id-123", updatedContact);

        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void updateNonExistentContactTest () {
        when(contactRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.updateContact("nonexistent-id", testContact));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
    }

    @Test
    void deleteContactTest () {
        when(contactRepository.existsById("test-id-123")).thenReturn(true);
        doNothing().when(contactRepository).deleteById("test-id-123");
        contactService.deleteContact("test-id-123");
        verify(contactRepository).deleteById("test-id-123");
    }

    @Test
    void deleteNonExistentContactTest () {
        when(contactRepository.existsById("nonexistent-id")).thenReturn(false);
        ContanctNotFoundException actualException = assertThrows(ContanctNotFoundException.class,
                                                                 () -> contactService.deleteContact("nonexistent-id"));
        assertEquals("Contact with id nonexistent-id not found", actualException.getMessage());
        verify(contactRepository, never()).deleteById(anyString());
    }
}