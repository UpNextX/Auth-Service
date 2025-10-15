package org.upnext.authservice.validation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldsMatchList {
    FieldsMatch[] value();
}
