package com.example.ex4.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OtherJobTitleValidatorImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OtherJobTitleValidator {
    String message() default "שם התפקיד חייב להכיל לפחות 2 תווים כאשר נבחר 'אחר'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
