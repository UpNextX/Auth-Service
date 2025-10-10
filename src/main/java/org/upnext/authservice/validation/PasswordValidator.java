package org.upnext.authservice.validation;

import jakarta.validation.Payload;

import java.lang.annotation.Documented;

@Documented
public @interface PasswordValidator {
    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
