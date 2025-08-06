package com.example.ex4.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {
    
    private long maxSize;
    
    @Value("${spring.servlet.multipart.max-file-size:1MB}")
    private String configMaxFileSize;
    
    /**
     * Initializes the validator with constraint annotation
     * 
     * @param constraintAnnotation FileSize annotation
     */
    @Override
    public void initialize(FileSize constraintAnnotation) {
        if (constraintAnnotation.max() == -1) {
            this.maxSize = parseFileSize(configMaxFileSize);
        } else {
            this.maxSize = constraintAnnotation.max();
        }
    }
    
    /**
     * Validates file size
     * 
     * @param file File to validate
     * @param context Validation context
     * @return true if file size is valid, false otherwise
     */
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        return file.getSize() <= maxSize;
    }
    
    /**
     * Parses file size string to bytes
     * 
     * @param size File size string (e.g., "1MB", "512KB")
     * @return Size in bytes
     */
    private long parseFileSize(String size) {
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        } else {
            return Long.parseLong(size);
        }
    }
}