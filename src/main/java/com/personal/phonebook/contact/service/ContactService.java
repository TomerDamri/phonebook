package com.personal.phonebook.contact.service;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.personal.phonebook.contact.controller.response.ContactsResponse;
import com.personal.phonebook.contact.model.Contact;
import com.personal.phonebook.contact.repository.ContactRepository;
import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.exception.PhonebookException;
import com.personal.phonebook.metric.ContactMetricsImpl;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContactService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("firstName", "lastName", "address");

    private static final Set<String> ALLOWED_SORT_DIRECTION = Set.of("ASC", "DESC");

    @Value("${phonebook.pagination.max-page-size}")
    private int maxPageSize;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactMetricsImpl contactMetricsImpl;

    @PostConstruct
    public void initializeMetrics () {
        contactMetricsImpl.setTotalContacts(contactRepository.count());
    }

    @Timed(value = "search_contacts_timer", description = "Time taken to search contacts")
    public ContactsResponse searchContacts (String query, int page, int size, String direction, String sortBy) {
        validatePageSize(size);
        validateSortField(sortBy);
        Sort sort = createSort(direction, sortBy);
        log.debug("Searching contacts with query: {}, page: {}, size: {}, direction: {}, sortBy: {}", query, page, size, direction, sortBy);
        if (query == null || query.isEmpty()) {
            return getContacts(page, size, sort);
        }
        Page<Contact> searchResult = contactMetricsImpl.getContactOperationTimer().record( () -> {
            Page<Contact> contacts = contactRepository.searchContacts(query, PageRequest.of(page, size, sort));
            contactMetricsImpl.incrementContactSearch();
            return contacts;
        });
        return createContactsResponse(searchResult);
    }

    @Timed(value = "create_contact_timer", description = "Time taken to create a contact")
    public Contact createContact (Contact contact) {
        validateContact(contact);
        log.debug("Creating new contact: {}", contact);
        Contact savedContact = contactMetricsImpl.getContactOperationTimer().record( () -> contactRepository.save(contact));
        if (savedContact == null) {
            throw new PhonebookException("Failed to create contact");
        }
        contactMetricsImpl.incrementContactCreated();
        log.debug("Contact created successfully with id: {}", savedContact.getId());
        return savedContact;
    }

    @Timed(value = "update_contact_timer", description = "Time taken to update a contact")
    public Contact updateContact (String id, Contact contactDetails) {
        validateContact(contactDetails);
        log.debug("Attempting to update contact with id: {}", id);
        Contact existingContact = requireById(id);
        updateContactDetails(contactDetails, existingContact);
        Contact updatedContact = contactMetricsImpl.getContactOperationTimer().record( () -> contactRepository.save(existingContact));
        contactMetricsImpl.incrementContactUpdated();
        log.debug("Contact updated successfully: {}", updatedContact);
        return updatedContact;
    }

    @Timed(value = "delete_contact_timer", description = "Time taken to delete a contact")
    public void deleteContact (String id) {
        log.debug("Attempting to delete contact with id: {}", id);
        if (contactRepository.existsById(id)) {
            contactMetricsImpl.getContactOperationTimer().record( () -> contactRepository.deleteById(id));
            contactMetricsImpl.incrementContactDeleted();
            log.debug("Contact deleted successfully with id: {}", id);
        }
        else {
            throw new ContanctNotFoundException(id);
        }
    }

    private void validateContact (Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        if (!StringUtils.hasText(contact.getFirstName())) {
            throw new IllegalArgumentException("First name is mandatory");
        }
        if (!StringUtils.hasText(contact.getPhone())) {
            throw new IllegalArgumentException("Phone number is mandatory");
        }
    }

    private Contact requireById (String id) {
        return contactRepository.findById(id).orElseThrow( () -> new ContanctNotFoundException(id));
    }

    private void validatePageSize (int size) {
        if (size > maxPageSize) {
            throw new IllegalArgumentException("Page size cannot be larger than " + maxPageSize);
        }
    }

    private Sort createSort (String direction, String sortBy) {
        try {
            Sort.Direction sortDirection = Sort.Direction.valueOf(direction.toUpperCase());
            return Sort.by(sortDirection, sortBy);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid sort direction '%s'. Allowed values are: %s", direction, String.join(", ", ALLOWED_SORT_FIELDS)));
        }
    }

    private void validateSortField (String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field. Allowed fields are: " + String.join(", ", ALLOWED_SORT_DIRECTION));
        }
    }

    private ContactsResponse getContacts (int page, int size, Sort sort) {
        Page<Contact> result = contactMetricsImpl.getContactOperationTimer()
                                                 .record( () -> contactRepository.findAll(PageRequest.of(page, size, sort)));
        return createContactsResponse(result);
    }

    private static ContactsResponse createContactsResponse (Page<Contact> searchResult) {
        return searchResult != null ? new ContactsResponse(searchResult.getContent(), searchResult.getTotalElements())
                    : new ContactsResponse(Collections.emptyList(), 0);
    }

    private static void updateContactDetails (Contact contactDetails, Contact existingContact) {
        existingContact.setFirstName(contactDetails.getFirstName());
        existingContact.setLastName(contactDetails.getLastName());
        existingContact.setPhone(contactDetails.getPhone());
        if (contactDetails.getAddress() != null) {
            existingContact.setAddress(contactDetails.getAddress());
        }
    }
}