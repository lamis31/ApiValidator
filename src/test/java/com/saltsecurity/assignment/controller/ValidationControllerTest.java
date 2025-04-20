package com.saltsecurity.assignment.controller;

import com.saltsecurity.assignment.model.*;
import com.saltsecurity.assignment.service.ModelStorageService;
import com.saltsecurity.assignment.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ValidationControllerTest {

    @Mock
    private ModelStorageService modelStorageService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ValidationController validationController;

    @Test
    void loadModels_validModels_shouldReturnOk() {
        // Setup
        ApiModel model1 = new ApiModel("/test1", "GET", null, null, null);
        ApiModel model2 = new ApiModel("/test2", "POST", null, null, null);
        List<ApiModel> models = Arrays.asList(model1, model2);
        
        doNothing().when(modelStorageService).storeModels(models);
        
        // Execute
        ResponseEntity<String> response = validationController.loadModels(models);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Successfully loaded 2 models"));
        verify(modelStorageService).storeModels(models);
    }


    @Test
    void loadModels_nullModels_shouldHandleGracefully() {
        // Execute
        ResponseEntity<String> response = validationController.loadModels(null);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Successfully loaded 0 models"));
        verify(modelStorageService).storeModels(null);
    }

    @Test
    void loadModels_serviceThrowsException_shouldReturnInternalServerError() {
        // Setup
        ApiModel model = new ApiModel("/test", "GET", null, null, null);
        List<ApiModel> models = Collections.singletonList(model);
        
        doThrow(new RuntimeException("Storage error")).when(modelStorageService).storeModels(models);
        
        // Execute
        ResponseEntity<String> response = validationController.loadModels(models);
        
        // Verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Error loading models"));
        verify(modelStorageService).storeModels(models);
    }

    @Test
    void validateRequest_validRequest_shouldReturnOkWithValidResult() {
        // Setup
        RequestData requestData = new RequestData("/test", "GET", null, null, null);
        ValidationResult validResult = new ValidationResult("valid", Collections.emptyList());
        
        when(validationService.validateRequest(requestData)).thenReturn(validResult);
        
        // Execute
        ResponseEntity<ValidationResult> response = validationController.validateRequest(requestData);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("valid", response.getBody().getStatus());
        assertTrue(response.getBody().getAnomalies().isEmpty());
        verify(validationService).validateRequest(requestData);
    }

    @Test
    void validateRequest_invalidRequest_shouldStillReturnOkWithAbnormalResult() {
        // Setup
        RequestData requestData = new RequestData("/test", "GET", null, null, null);
        List<Anomaly> anomalies = Collections.singletonList(
                new Anomaly("query_params[param1]", "Missing required parameter"));
        ValidationResult abnormalResult = new ValidationResult("abnormal", anomalies);
        
        when(validationService.validateRequest(requestData)).thenReturn(abnormalResult);
        
        // Execute
        ResponseEntity<ValidationResult> response = validationController.validateRequest(requestData);
        
        // Verify - should still be 200 OK as per assignment requirements
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("abnormal", response.getBody().getStatus());
        assertEquals(1, response.getBody().getAnomalies().size());
        verify(validationService).validateRequest(requestData);
    }

    @Test
    void validateRequest_serviceThrowsException_shouldReturnInternalServerError() {
        // Setup
        RequestData requestData = new RequestData("/test", "GET", null, null, null);
        
        when(validationService.validateRequest(requestData)).thenThrow(new RuntimeException("Validation error"));
        
        // Execute
        ResponseEntity<ValidationResult> response = validationController.validateRequest(requestData);
        
        // Verify
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(validationService).validateRequest(requestData);
    }
} 