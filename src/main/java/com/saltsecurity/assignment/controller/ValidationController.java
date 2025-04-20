package com.saltsecurity.assignment.controller;

import com.saltsecurity.assignment.model.ApiModel;
import com.saltsecurity.assignment.model.RequestData;
import com.saltsecurity.assignment.model.ValidationResult;
import com.saltsecurity.assignment.service.ModelStorageService;
import com.saltsecurity.assignment.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Includes @RestController, @PostMapping, @RequestBody, etc.

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api") // Sets a base path for all endpoints in this controller
@RequiredArgsConstructor
public class ValidationController {

    private final ModelStorageService modelStorageService;
    private final ValidationService validationService;

    /**
     * Endpoint to receive and store API models.
     * Expects a JSON list of ApiModel objects in the request body.
     * Example URL: POST http://localhost:8080/api/models
     *
     * @param models List of ApiModel objects from the request body.
     * @return A ResponseEntity indicating success.
     */
    @PostMapping("/models")
    public ResponseEntity<String> loadModels(@RequestBody List<ApiModel> models) {
        try {
            modelStorageService.storeModels(models);
            // You could return the number of models loaded, etc.
            return ResponseEntity.ok("Successfully loaded " + (models != null ? models.size() : 0) + " models.");
        } catch (Exception e) {
            log.error("Error loading models: ", e);
            return ResponseEntity.internalServerError().body("Error loading models: " + e.getMessage());
        }
    }

    /**
     * Endpoint to validate incoming request data against stored models.
     * Expects a JSON RequestData object in the request body.
     * Returns a JSON ValidationResult object with status 200 OK, as per the
     * assignment.
     * Example URL: POST http://localhost:8080/api/validate
     *
     * @param requestData The RequestData object from the request body.
     * @return A ValidationResult object (automatically serialized to JSON).
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateRequest(@RequestBody RequestData requestData) {
        try {
            ValidationResult result = validationService.validateRequest(requestData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during validation: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint to validate multiple requests in a single batch.
     * Expects a JSON array of RequestData objects in the request body.
     * Returns a JSON array of ValidationResult objects with status 200 OK.
     * Example URL: POST http://localhost:8080/api/validate/batch
     *
     * @param requestDataList List of RequestData objects from the request body.
     * @return A list of ValidationResult objects (automatically serialized to JSON).
     */
    @PostMapping("/validate/batch")
    public ResponseEntity<List<ValidationResult>> validateBatch(@RequestBody List<RequestData> requestDataList) {
        try {
            List<ValidationResult> results = requestDataList.stream()
                .map(validationService::validateRequest)
                .toList();
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error during batch validation: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
