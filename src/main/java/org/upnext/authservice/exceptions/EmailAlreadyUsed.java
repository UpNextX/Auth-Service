package org.upnext.authservice.exceptions;

public class EmailAlreadyUsed extends RuntimeException {
    public EmailAlreadyUsed(String message) {
        super(message);
    }
}
