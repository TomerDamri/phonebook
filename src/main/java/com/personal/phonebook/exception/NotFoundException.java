package com.personal.phonebook.exception;

public abstract class NotFoundException extends PhonebookException {

    public NotFoundException (Class<?> entityType, String entityId) {
        this(entityType, entityId, null);
    }

    public NotFoundException (Class<?> entityType, String entityId, String additionalInfo) {
        super(buildMessage(entityType.getSimpleName(), entityId, additionalInfo));
    }

    private static String buildMessage (String entityType, String entityId, String additionalInfo) {
        StringBuilder message = new StringBuilder().append(entityType).append(" with id ").append(entityId).append(" not found");

        if (additionalInfo != null) {
            message.append(" (").append(additionalInfo).append(")");
        }

        return message.toString();
    }
}