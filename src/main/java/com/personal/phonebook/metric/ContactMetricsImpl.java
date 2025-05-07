package com.personal.phonebook.metric;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;

@Component
public class ContactMetricsImpl {

    private static final String PHONEBOOK_CONTACTS_CREATED = "phonebook.contacts.created";
    private static final String CONTACTS_CREATED_DESCRIPTION = "Number of contacts created";
    private static final String PHONEBOOK_CONTACTS_UPDATED = "phonebook.contacts.updated";
    private static final String CONTACTS_UPDATED_DESCRIPTION = "Number of contacts updated";
    private static final String PHONEBOOK_CONTACTS_DELETED = "phonebook.contacts.deleted";
    private static final String CONTACTS_DELETED_DESCRIPTION = "Number of contacts deleted";
    private static final String PHONEBOOK_CONTACTS_SEARCH = "phonebook.contacts.search";
    private static final String CONTACT_SEARCH_OPERATIONS_DESCRIPTION = "Number of contact search operations";
    private static final String PHONEBOOK_OPERATION_TIMER = "phonebook.operation.timer";
    private static final String CONTACT_OPERATIONS_DESCRIPTION = "Timer for contact operations";
    private static final String TYPE = "type";
    private static final String REPOSITORY_OPERATION = "repository_operation";
    private static final String PHONEBOOK_CONTACTS_COUNT = "phonebook.contacts.count";
    private static final String CURRENT_CONTACTS_NUMBER_DESCRIPTION = "Current number of contacts";
    private final Counter contactCreatedCounter;
    private final Counter contactUpdatedCounter;
    private final Counter contactDeletedCounter;
    private final Counter contactSearchCounter;
    @Getter
    private final Timer contactOperationTimer;
    private final AtomicLong totalContacts;

    @Autowired
    public ContactMetricsImpl (MeterRegistry registry) {
        // Contact operation counters
        this.contactCreatedCounter = Counter.builder(PHONEBOOK_CONTACTS_CREATED)
                                            .description(CONTACTS_CREATED_DESCRIPTION)
                                            .register(registry);

        this.contactUpdatedCounter = Counter.builder(PHONEBOOK_CONTACTS_UPDATED)
                                            .description(CONTACTS_UPDATED_DESCRIPTION)
                                            .register(registry);

        this.contactDeletedCounter = Counter.builder(PHONEBOOK_CONTACTS_DELETED)
                                            .description(CONTACTS_DELETED_DESCRIPTION)
                                            .register(registry);

        this.contactSearchCounter = Counter.builder(PHONEBOOK_CONTACTS_SEARCH)
                                           .description(CONTACT_SEARCH_OPERATIONS_DESCRIPTION)
                                           .register(registry);

        // Operation timer
        this.contactOperationTimer = Timer.builder(PHONEBOOK_OPERATION_TIMER)
                                          .description(CONTACT_OPERATIONS_DESCRIPTION)
                                          .tag(TYPE, REPOSITORY_OPERATION)
                                          .register(registry);

        // Current contacts gauge
        this.totalContacts = new AtomicLong(0);

        Gauge.builder(PHONEBOOK_CONTACTS_COUNT, totalContacts::get).description(CURRENT_CONTACTS_NUMBER_DESCRIPTION).register(registry);
    }

    public void incrementContactCreated () {
        contactCreatedCounter.increment();
        totalContacts.incrementAndGet();
    }

    public void incrementContactUpdated () {
        contactUpdatedCounter.increment();
    }

    public void incrementContactDeleted () {
        contactDeletedCounter.increment();
        totalContacts.decrementAndGet();
    }

    public void incrementContactSearch () {
        contactSearchCounter.increment();
    }

    public void setTotalContacts (long count) {
        totalContacts.set(count);
    }
}
