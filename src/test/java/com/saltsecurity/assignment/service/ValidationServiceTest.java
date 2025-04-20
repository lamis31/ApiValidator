package com.saltsecurity.assignment.service;

import com.saltsecurity.assignment.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private ModelStorageService modelStorageService;

    @InjectMocks
    private ValidationService validationService;

    @Test
    void validateRequest_nullRequestData_shouldReturnAbnormal() {
        ValidationResult result = validationService.validateRequest(null);

        assertEquals("abnormal", result.getStatus());
        assertEquals(1, result.getAnomalies().size());
        assertEquals("request", result.getAnomalies().get(0).getField());
        assertEquals("Invalid request data (null or missing path/method)", result.getAnomalies().get(0).getReason());
    }

    @Test
    void validateRequest_nullMethod_shouldReturnAbnormal() {
        RequestData requestData = new RequestData("/test", null, null, null, null);
        ValidationResult result = validationService.validateRequest(requestData);

        assertEquals("abnormal", result.getStatus());
        assertEquals(1, result.getAnomalies().size());
        assertEquals("request", result.getAnomalies().get(0).getField());
    }

    @Test
    void validateRequest_nullPath_shouldReturnAbnormal() {
        RequestData requestData = new RequestData(null, "GET", null, null, null);
        ValidationResult result = validationService.validateRequest(requestData);

        assertEquals("abnormal", result.getStatus());
        assertEquals(1, result.getAnomalies().size());
        assertEquals("request", result.getAnomalies().get(0).getField());
    }

    @Test
    void validateRequest_modelNotFound_shouldReturnAbnormal() {
        RequestData requestData = new RequestData("/test", "GET", null, null, null);
        
        when(modelStorageService.findModel("GET", "/test")).thenReturn(Optional.empty());
        
        ValidationResult result = validationService.validateRequest(requestData);
        
        assertEquals("abnormal", result.getStatus());
        assertFalse(result.getAnomalies().isEmpty());
        assertEquals(1, result.getAnomalies().size());
        assertEquals("request", result.getAnomalies().get(0).getField());
        assertEquals("No API model defined for this endpoint", result.getAnomalies().get(0).getReason());
        
        verify(modelStorageService).findModel("GET", "/test");
    }

    @Test
    void validateRequest_noAnomalies_shouldReturnValid() {
        // Setup request data
        RequestData requestData = new RequestData(
            "/test", 
            "GET", 
            Collections.singletonList(new ParamValue("query1", "123")),
            Collections.singletonList(new ParamValue("header1", "value1")),
            Collections.singletonList(new ParamValue("body1", "true"))
        );
        
        // Setup model data
        ApiModel model = new ApiModel(
            "/test",
            "GET",
            Collections.singletonList(new ParamDefinition("query1", Collections.singletonList("int"), true)),
            Collections.singletonList(new ParamDefinition("header1", Collections.singletonList("string"), true)),
            Collections.singletonList(new ParamDefinition("body1", Collections.singletonList("boolean"), true))
        );
        
        when(modelStorageService.findModel("GET", "/test")).thenReturn(Optional.of(model));
        
        ValidationResult result = validationService.validateRequest(requestData);
        
        assertEquals("valid", result.getStatus());
        assertTrue(result.getAnomalies().isEmpty());
        
        verify(modelStorageService).findModel("GET", "/test");
    }

    @Test
    void validateRequest_withAnomalies_shouldReturnAbnormal() {
        // Setup request data with missing and invalid parameters
        RequestData requestData = new RequestData(
            "/test", 
            "GET", 
            Collections.singletonList(new ParamValue("query1", "not-a-number")), // Type mismatch
            Collections.emptyList(),  // Missing required header
            Collections.singletonList(new ParamValue("body1", "true"))
        );
        
        // Setup model data
        ApiModel model = new ApiModel(
            "/test",
            "GET",
            Collections.singletonList(new ParamDefinition("query1", Collections.singletonList("int"), true)),
            Collections.singletonList(new ParamDefinition("header1", Collections.singletonList("string"), true)),
            Collections.singletonList(new ParamDefinition("body1", Collections.singletonList("boolean"), true))
        );
        
        when(modelStorageService.findModel("GET", "/test")).thenReturn(Optional.of(model));
        
        ValidationResult result = validationService.validateRequest(requestData);
        
        assertEquals("abnormal", result.getStatus());
        assertEquals(2, result.getAnomalies().size());
        
        // Check for specific anomalies
        boolean foundQueryTypeMismatch = false;
        boolean foundHeaderMissing = false;
        
        for (Anomaly anomaly : result.getAnomalies()) {
            if (anomaly.getField().equals("query_params[query1]") && anomaly.getReason().contains("Type mismatch")) {
                foundQueryTypeMismatch = true;
            } else if (anomaly.getField().equals("headers[header1]") && anomaly.getReason().equals("Missing required parameter")) {
                foundHeaderMissing = true;
            }
        }
        
        assertTrue(foundQueryTypeMismatch);
        assertTrue(foundHeaderMissing);
        
        verify(modelStorageService).findModel("GET", "/test");
    }

    @Test
    void validateRequest_withNullCollections_shouldHandleGracefully() {
        // Setup request data with null collections
        RequestData requestData = new RequestData("/test", "GET", null, null, null);
        
        // Setup model data with required parameters
        ApiModel model = new ApiModel(
            "/test",
            "GET",
            Collections.singletonList(new ParamDefinition("query1", Collections.singletonList("string"), true)),
            Collections.singletonList(new ParamDefinition("header1", Collections.singletonList("string"), true)),
            Collections.singletonList(new ParamDefinition("body1", Collections.singletonList("string"), true))
        );
        
        when(modelStorageService.findModel("GET", "/test")).thenReturn(Optional.of(model));
        
        ValidationResult result = validationService.validateRequest(requestData);
        
        assertEquals("abnormal", result.getStatus());
        assertEquals(3, result.getAnomalies().size());
        
        verify(modelStorageService).findModel("GET", "/test");
    }
} 