package com.saltsecurity.assignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResult {
    private String status;
    private List<Anomaly> anomalies;
}
