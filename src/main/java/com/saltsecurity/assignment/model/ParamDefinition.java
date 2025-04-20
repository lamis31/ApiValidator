package com.saltsecurity.assignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamDefinition {
    private String name;
    private List<String> types; // Allowed types like "String", "Int", "Email" 
    private Boolean required;   // Is this parameter required?
  
}
