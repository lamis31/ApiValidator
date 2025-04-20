package com.saltsecurity.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParamValue {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private String name;
    private Object value; // Can be String, Number, Boolean, Array, or Object
    
    /**
     * Returns the value as a string, converting complex objects to JSON strings
     * @return The value as a string
     */
    @JsonIgnore
    public String getValueAsString() {
        if (value == null) {
            return null;
        }
        
        if (value instanceof String) {
            return (String) value;
        }
        
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
