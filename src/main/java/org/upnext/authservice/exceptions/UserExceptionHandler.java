package org.upnext.authservice.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsed.class)
    public ResponseEntity<?> handleEmailAlreadyUsed(EmailAlreadyUsed e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(TokenNotFound.class)
    public ResponseEntity<?> handleTokenNotFound(TokenNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
