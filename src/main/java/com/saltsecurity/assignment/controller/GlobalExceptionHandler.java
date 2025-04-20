package com.saltsecurity.assignment.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API to provide consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle JSON parsing errors (when request body can't be deserialized to target objects)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        
        // Get the root cause to provide more specific error messages
        Throwable rootCause = ex.getRootCause();
        
        if (rootCause instanceof JsonParseException) {
            errorResponse.put("message", "Invalid JSON syntax in request body");
        } else if (rootCause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) rootCause;
            errorResponse.put("message", "Invalid format for field: " + ife.getPath().get(0).getFieldName());
            errorResponse.put("expected_type", ife.getTargetType().getSimpleName());
            errorResponse.put("actual_value", ife.getValue().toString());
        } else if (rootCause instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) rootCause;
            String fieldName = mie.getPath().isEmpty() ? "unknown" : mie.getPath().get(0).getFieldName();
            errorResponse.put("message", "Type mismatch for field: " + fieldName);
            errorResponse.put("expected_type", mie.getTargetType().getSimpleName());
        } else {
            errorResponse.put("message", "Invalid JSON format or structure in request body");
        }
        
        // Add detailed error message for debugging
        errorResponse.put("details", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle general server errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "An unexpected server error occurred");
        
        // In a production environment, you might not want to expose the full exception message
        // But it's useful for debugging during development
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("exception_type", ex.getClass().getSimpleName());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 