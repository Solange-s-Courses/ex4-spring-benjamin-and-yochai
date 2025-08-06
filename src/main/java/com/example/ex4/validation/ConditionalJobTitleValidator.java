package com.example.ex4.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalJobTitleValidator.ConditionalJobTitleValidatorImpl.class)
public @interface ConditionalJobTitleValidator {
    String message() default "חובה לבחור תפקיד מהרשימה או להזין תפקיד אחר";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class ConditionalJobTitleValidatorImpl implements ConstraintValidator<ConditionalJobTitleValidator, Object> {
        @Override
        public boolean isValid(Object obj, ConstraintValidatorContext context) {
            if (obj == null) {
                return true;
            }
            
            try {
                Field jobTitleField = obj.getClass().getDeclaredField("jobTitle");
                Field otherJobTitleField = obj.getClass().getDeclaredField("otherJobTitle");
                
                jobTitleField.setAccessible(true);
                otherJobTitleField.setAccessible(true);
                
                String jobTitle = (String) jobTitleField.get(obj);
                String otherJobTitle = (String) otherJobTitleField.get(obj);
                
                if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                    return true;
                }
                
                if (otherJobTitle != null && !otherJobTitle.trim().isEmpty() && otherJobTitle.trim().length() >= 2) {
                    return true;
                }
                
                return false;
                
            } catch (Exception e) {
                return false;
            }
        }
    }
} 