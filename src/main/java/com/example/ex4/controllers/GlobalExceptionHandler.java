package com.example.ex4.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all unhandled exceptions
     * 
     * @param exc The exception that occurred
     * @return The name of the error page template
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAnyException(Exception exc) {
        System.out.println("InvalidDataAccessApiUsageException at " + exc.getMessage() + ": " + exc);
        return "error";
    }
}