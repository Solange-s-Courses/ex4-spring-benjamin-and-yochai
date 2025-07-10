package com.example.ex4.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileSizeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileSize {
    String message() default "גודל הקובץ חורג מהמותר";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    long max() default -1;
}