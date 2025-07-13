package com.example.ex4.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequirementsValidator.RequirementsValidatorImpl.class)
public @interface RequirementsValidator {
    String message() default "כל דרישה חייבת להכיל לפחות 3 תווים";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class RequirementsValidatorImpl implements ConstraintValidator<RequirementsValidator, List<String>> {
        @Override
        public boolean isValid(List<String> requirements, ConstraintValidatorContext context) {
            if (requirements == null || requirements.isEmpty()) {
                return false;
            }
            
            for (String requirement : requirements) {
                if (requirement == null || requirement.trim().length() < 3) {
                    return false;
                }
            }
            return true;
        }
    }
} 