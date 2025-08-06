package com.example.ex4.validation;

import com.example.ex4.dto.PositionForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OtherJobTitleValidatorImpl implements ConstraintValidator<OtherJobTitleValidator, PositionForm> {

    /**
     * Initializes the validator
     * 
     * @param constraintAnnotation OtherJobTitleValidator annotation
     */
    @Override
    public void initialize(OtherJobTitleValidator constraintAnnotation) {
        // No initialization needed
    }

    /**
     * Validates other job title when "other" is selected
     * 
     * @param positionForm Position form to validate
     * @param context Validation context
     * @return true if validation passes, false otherwise
     */
    @Override
    public boolean isValid(PositionForm positionForm, ConstraintValidatorContext context) {
        if (positionForm == null) {
            return true;
        }

        String jobTitle = positionForm.getJobTitle();
        String otherJobTitle = positionForm.getOtherJobTitle();

        // If jobTitle is "other", validate otherJobTitle
        if ("אחר".equals(jobTitle)) {
            if (otherJobTitle == null || otherJobTitle.trim().isEmpty()) {
                // Add constraint violation to the otherJobTitle field
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("שם התפקיד חייב להכיל לפחות 2 תווים")
                        .addPropertyNode("otherJobTitle")
                        .addConstraintViolation();
                return false;
            }

            if (otherJobTitle.trim().length() < 2) {
                // Add constraint violation to the otherJobTitle field
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("שם התפקיד חייב להכיל לפחות 2 תווים")
                        .addPropertyNode("otherJobTitle")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}