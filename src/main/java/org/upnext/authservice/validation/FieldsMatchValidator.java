package org.upnext.authservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldsMatchValidator  implements ConstraintValidator<FieldsMatch, Object> {
    String first;
    String second;
    String message;
    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        this.first = constraintAnnotation.first();
        this.second = constraintAnnotation.second();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        Object firstValue = new BeanWrapperImpl(o).getPropertyValue(first);
        Object secondValue = new BeanWrapperImpl(o).getPropertyValue(second);
        boolean valid = (firstValue == null && secondValue == null)
                || (firstValue != null && firstValue.equals(secondValue));
        if (!valid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(second)
                    .addConstraintViolation();
        }

        return valid;

    }
}
