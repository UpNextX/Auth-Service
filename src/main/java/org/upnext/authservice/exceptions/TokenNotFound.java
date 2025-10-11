package org.upnext.authservice.exceptions;

public class TokenNotFound extends RuntimeException{
    public TokenNotFound(String message) {
        super(message);
    }
}
