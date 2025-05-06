package com.personal.phonebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.personal.phonebook.controller.response.ContactsResponse;
import com.personal.phonebook.exception.ContanctNotFoundException;
import com.personal.phonebook.model.Contact;
import com.personal.phonebook.repository.ContactRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContactService {

    private static final String FIRST_NAME = "firstName";
    private static final Sort SORT = Sort.by(Sort.Direction.ASC, FIRST_NAME);
    @Value("${phonebook.pagination.max-page-size}")
    private int maxPageSize;

    @Autowired
    private ContactRepository contactRepository;

    public ContactsResponse searchContacts (String query, int page, int size) {
        validatePageSize(size);
        if (query == null || query.isEmpty()) {
            return getContacts(page, size);
        }
        Page<Contact> result = contactRepository.searchContacts(query, PageRequest.of(page, size, SORT));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }

    public Contact createContact (Contact contact) {
        validateContact(contact);
        log.debug("Creating new contact: {}", contact);
        Contact savedContact = contactRepository.save(contact);
        log.debug("Contact created successfully with id: {}", savedContact.getId());
        return savedContact;
    }

    public Contact updateContact (String id, Contact contactDetails) {
        validateContact(contactDetails);
        log.debug("Attempting to update contact with id: {}", id);
        Contact existingContact = requireById(id);
        existingContact.setFirstName(contactDetails.getFirstName());
        existingContact.setLastName(contactDetails.getLastName());
        existingContact.setPhone(contactDetails.getPhone());
        existingContact.setAddress(contactDetails.getAddress());
        Contact updatedContact = contactRepository.save(existingContact);
        log.debug("Contact updated successfully: {}", updatedContact);
        return updatedContact;
    }

    public void deleteContact (String id) {
        log.debug("Attempting to delete contact with id: {}", id);
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
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

    private ContactsResponse getContacts (int page, int size) {
        Page<Contact> result = contactRepository.findAll(PageRequest.of(page, size, SORT));
        return new ContactsResponse(result.getContent(), result.getTotalElements());
    }
}