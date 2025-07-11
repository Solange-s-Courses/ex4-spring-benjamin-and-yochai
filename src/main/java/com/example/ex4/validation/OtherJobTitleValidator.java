package com.example.ex4.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OtherJobTitleValidator.OtherJobTitleValidatorImpl.class)
public @interface OtherJobTitleValidator {
    String message() default "חובה להזין תפקיד אחר";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class OtherJobTitleValidatorImpl implements ConstraintValidator<OtherJobTitleValidator, String> {
        @Override
        public boolean isValid(String otherJobTitle, ConstraintValidatorContext context) {
            // אם השדה ריק, זה תקין (כי הוא לא תמיד נדרש)
            if (otherJobTitle == null || otherJobTitle.trim().isEmpty()) {
                return true;
            }
            
            // אם יש תוכן, בודק שהוא לפחות 2 תווים
            return otherJobTitle.trim().length() >= 2;
        }
    }
}