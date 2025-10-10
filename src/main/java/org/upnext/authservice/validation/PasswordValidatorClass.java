package org.upnext.authservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidatorClass implements ConstraintValidator<PasswordValidator, String> {
    @Override
    public void initialize(PasswordValidator constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,32}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if(password == null){
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }
}
