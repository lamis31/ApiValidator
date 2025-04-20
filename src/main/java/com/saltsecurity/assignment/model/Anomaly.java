package com.saltsecurity.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Anomaly {
    private String field; // e.g., "query_params[QueryParam1]", "header[HeaderParam1]"
    private String reason; // e.g., "Type mismatch", "Missing required parameter"
}
