package com.saltsecurity.assignment.service;

import com.saltsecurity.assignment.model.Anomaly;
import com.saltsecurity.assignment.model.ParamDefinition;
import com.saltsecurity.assignment.model.ParamValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParameterValidatorTest {
    
    @Test
    void validateParameterGroup_nullDefinitions_shouldReturnEmptyList() {
        List<Anomaly> result = ParameterValidator.validateParameterGroup(null, 
                Collections.singletonList(new ParamValue("param1", "value1")), 
                "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_emptyDefinitions_shouldReturnEmptyList() {
        List<Anomaly> result = ParameterValidator.validateParameterGroup(Collections.emptyList(), 
                Collections.singletonList(new ParamValue("param1", "value1")), 
                "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_nullValues_shouldHandleGracefully() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("string"), true);
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), null, "test");
        
        assertEquals(1, result.size());
        assertEquals("test[param1]", result.get(0).getField());
        assertEquals("Missing required parameter", result.get(0).getReason());
    }
    
    @Test
    void validateParameterGroup_requiredParamMissing_shouldAddAnomaly() {
        ParamDefinition def1 = new ParamDefinition("param1", Collections.singletonList("string"), true);
        ParamDefinition def2 = new ParamDefinition("param2", Collections.singletonList("string"), true);
        
        List<ParamValue> values = Collections.singletonList(
                new ParamValue("param1", "value1")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Arrays.asList(def1, def2), values, "test");
        
        assertEquals(1, result.size());
        assertEquals("test[param2]", result.get(0).getField());
        assertEquals("Missing required parameter", result.get(0).getReason());
    }
    
    @Test
    void validateParameterGroup_optionalParamMissing_shouldNotAddAnomaly() {
        ParamDefinition def1 = new ParamDefinition("param1", Collections.singletonList("string"), true);
        ParamDefinition def2 = new ParamDefinition("param2", Collections.singletonList("string"), false);
        
        List<ParamValue> values = Collections.singletonList(
                new ParamValue("param1", "value1")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Arrays.asList(def1, def2), values, "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_typeMismatch_shouldAddAnomaly() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("int"), false);
        
        List<ParamValue> values = Collections.singletonList(
                new ParamValue("param1", "not-a-number")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), values, "test");
        
        assertEquals(1, result.size());
        assertEquals("test[param1]", result.get(0).getField());
        assertTrue(result.get(0).getReason().contains("Type mismatch"));
    }
    
    @Test
    void validateParameterGroup_typeMatch_shouldNotAddAnomaly() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("int"), false);
        
        List<ParamValue> values = Collections.singletonList(
                new ParamValue("param1", "123")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), values, "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_multipleAnomalies_shouldAddAllAnomalies() {
        ParamDefinition def1 = new ParamDefinition("param1", Collections.singletonList("int"), false);
        ParamDefinition def2 = new ParamDefinition("param2", Collections.singletonList("string"), true);
        ParamDefinition def3 = new ParamDefinition("param3", Collections.singletonList("email"), false);
        
        List<ParamValue> values = Arrays.asList(
                new ParamValue("param1", "not-a-number"),
                new ParamValue("param3", "not-an-email")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Arrays.asList(def1, def2, def3), values, "test");
        
        assertEquals(3, result.size());
        
        // Check for all three expected anomalies
        boolean foundParam1TypeMismatch = false;
        boolean foundParam2Missing = false;
        boolean foundParam3TypeMismatch = false;
        
        for (Anomaly anomaly : result) {
            if (anomaly.getField().equals("test[param1]") && anomaly.getReason().contains("Type mismatch")) {
                foundParam1TypeMismatch = true;
            } else if (anomaly.getField().equals("test[param2]") && anomaly.getReason().equals("Missing required parameter")) {
                foundParam2Missing = true;
            } else if (anomaly.getField().equals("test[param3]") && anomaly.getReason().contains("Type mismatch")) {
                foundParam3TypeMismatch = true;
            }
        }
        
        assertTrue(foundParam1TypeMismatch);
        assertTrue(foundParam2Missing);
        assertTrue(foundParam3TypeMismatch);
    }
    
    @Test
    void validateParameterGroup_duplicateParameters_shouldUseFirstValue() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("int"), false);
        
        List<ParamValue> values = Arrays.asList(
                new ParamValue("param1", "123"),
                new ParamValue("param1", "not-a-number")
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), values, "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_nullParamValues_shouldHandleGracefully() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("string"), false);
        
        List<ParamValue> values = Arrays.asList(
                null,
                new ParamValue("param1", "value1")
        );
        
        // Should not throw exception
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), values, "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_paramNameNull_shouldHandleGracefully() {
        ParamDefinition def = new ParamDefinition("param1", Collections.singletonList("string"), false);
        
        List<ParamValue> values = Arrays.asList(
                new ParamValue(null, "value1"),
                new ParamValue("param1", "value2")
        );
        
        // Should not throw exception
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Collections.singletonList(def), values, "test");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void validateParameterGroup_unexpectedParameter_shouldAddAnomaly() {
        // Define expected parameters
        ParamDefinition def1 = new ParamDefinition("param1", Collections.singletonList("string"), false);
        ParamDefinition def2 = new ParamDefinition("param2", Collections.singletonList("int"), false);
        
        // Add expected and unexpected parameters
        List<ParamValue> values = Arrays.asList(
                new ParamValue("param1", "value1"),         // Expected parameter
                new ParamValue("param2", "123"),            // Expected parameter
                new ParamValue("unexpected_param", "xyz"),  // Unexpected parameter
                new ParamValue("another_unexpected", "abc") // Another unexpected parameter
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Arrays.asList(def1, def2), values, "query_params");
        
        // Should have 2 anomalies for the unexpected parameters
        assertEquals(2, result.size());
        
        boolean foundFirstUnexpected = false;
        boolean foundSecondUnexpected = false;
        
        for (Anomaly anomaly : result) {
            if (anomaly.getField().equals("query_params[unexpected_param]") && 
                anomaly.getReason().contains("Unexpected parameter")) {
                foundFirstUnexpected = true;
            } else if (anomaly.getField().equals("query_params[another_unexpected]") && 
                       anomaly.getReason().contains("Unexpected parameter")) {
                foundSecondUnexpected = true;
            }
        }
        
        assertTrue(foundFirstUnexpected, "Should detect first unexpected parameter");
        assertTrue(foundSecondUnexpected, "Should detect second unexpected parameter");
    }
    
    @Test
    void validateParameterGroup_mixedExpectedAndUnexpected_shouldReportAllIssues() {
        // Define expected parameters with one required
        ParamDefinition def1 = new ParamDefinition("param1", Collections.singletonList("string"), true);
        ParamDefinition def2 = new ParamDefinition("param2", Collections.singletonList("int"), false);
        
        // Add some values with issues
        List<ParamValue> values = Arrays.asList(
                new ParamValue("param2", "not-a-number"),       // Type mismatch
                new ParamValue("unexpected_param", "xyz")       // Unexpected parameter
                // param1 is missing (required)
        );
        
        List<Anomaly> result = ParameterValidator.validateParameterGroup(
                Arrays.asList(def1, def2), values, "body");
        
        // Should have 3 anomalies: missing required, type mismatch, unexpected param
        assertEquals(3, result.size());
        
        boolean foundMissingRequired = false;
        boolean foundTypeMismatch = false;
        boolean foundUnexpected = false;
        
        for (Anomaly anomaly : result) {
            if (anomaly.getField().equals("body[param1]") && 
                anomaly.getReason().equals("Missing required parameter")) {
                foundMissingRequired = true;
            } else if (anomaly.getField().equals("body[param2]") && 
                       anomaly.getReason().contains("Type mismatch")) {
                foundTypeMismatch = true;
            } else if (anomaly.getField().equals("body[unexpected_param]") && 
                       anomaly.getReason().contains("Unexpected parameter")) {
                foundUnexpected = true;
            }
        }
        
        assertTrue(foundMissingRequired, "Should detect missing required parameter");
        assertTrue(foundTypeMismatch, "Should detect type mismatch");
        assertTrue(foundUnexpected, "Should detect unexpected parameter");
    }
} 