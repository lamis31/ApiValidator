package com.saltsecurity.assignment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.saltsecurity.assignment.model.Anomaly;
import com.saltsecurity.assignment.model.ParamDefinition;
import com.saltsecurity.assignment.model.ParamValue;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for validating API parameters.
 */
@Slf4j
public class ParameterValidator {
    
    /**
     * Validates a group of parameters against their definitions.
     * Works for any parameter type (query parameters, headers, body elements).
     *
     * @param definitions The list of parameter definitions from the model.
     * @param values      The list of parameter values from the request.
     * @param location    String indicating the location (e.g., "query_params", "headers", "body") for anomaly reporting.
     * @return List of anomalies found during validation (empty if all valid).
     */
    public static List<Anomaly> validateParameterGroup(List<ParamDefinition> definitions, List<ParamValue> values, String location) {
        List<Anomaly> anomalies = new ArrayList<>();
        
        if (definitions == null || definitions.isEmpty()) {
            return anomalies; // No definitions to validate against, so no anomalies
        }
        
        if (values == null || values.isEmpty()) {
            // Check for missing required parameters when no values are provided
            for (ParamDefinition def : definitions) {
                if (Boolean.TRUE.equals(def.getRequired())) {
                    String paramFullName = formatParamName(location, def.getName());
                    anomalies.add(new Anomaly(paramFullName, "Missing required parameter"));
                }
            }
            return anomalies;
        }
        
        // Create a set of known parameter names for quick lookup
        Set<String> knownParams = definitions.stream()
                .map(ParamDefinition::getName)
                .collect(Collectors.toSet());
        
        // Convert request values to a map for quick lookup by name
        Map<String, String> valueMap = createValueMap(values);
        
        // 1. Check for defined parameters (required params, type validation)
        for (ParamDefinition def : definitions) {
            String paramName = def.getName();
            String paramFullName = formatParamName(location, paramName);
            String actualValue = valueMap.get(paramName);

            validateParameter(def, actualValue, paramFullName, anomalies);
        }
        
        // 2. Check for unexpected parameters not defined in the model
        for (ParamValue value : values) {
            if (value != null && value.getName() != null && !knownParams.contains(value.getName())) {
                String paramFullName = formatParamName(location, value.getName());
                anomalies.add(new Anomaly(paramFullName, "Unexpected parameter not defined in API model"));
                log.warn("Unexpected parameter detected: {} in {}", value.getName(), location);
            }
        }
        
        return anomalies;
    }
    
    /**
     * Validates a single parameter against its definition.
     */
    private static void validateParameter(ParamDefinition definition, String actualValue, 
            String paramFullName, List<Anomaly> anomalies) {
        // 1. Check for missing required parameters
        if (Boolean.TRUE.equals(definition.getRequired()) && actualValue == null) {
            anomalies.add(new Anomaly(paramFullName, "Missing required parameter"));
        } else if (actualValue != null) {
            // 2. Check for type mismatch if the parameter is present
            if (!ParameterTypeValidator.isValidType(actualValue, definition.getTypes())) {
                anomalies.add(new Anomaly(paramFullName,
                        "Type mismatch. Expected: " + definition.getTypes() + ", Actual Value: '" + actualValue + "'"));
            }
        }
    }
    
    /**
     * Creates a map of parameter names to values for efficient lookup.
     * if multiple values are present for the same parameter, the first one is used.
     */
    private static Map<String, String> createValueMap(List<ParamValue> values) {
        return (values == null) ? Collections.emptyMap()
                : values.stream()
                        .filter(v -> v != null && v.getName() != null)
                        .collect(Collectors.toMap(
                            ParamValue::getName, 
                            ParamValue::getValueAsString, 
                            (v1, v2) -> v1
                        ));
    }
    
    /**
     * Formats a parameter name with its location.
     */
    private static String formatParamName(String location, String paramName) {
        return location + "[" + paramName + "]"; // e.g., "query_params[Param1]"
    }
} 