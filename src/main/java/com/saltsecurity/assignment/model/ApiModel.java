package com.saltsecurity.assignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiModel {
    private String path;
    private String method;
    private List<ParamDefinition> query_params;
    private List<ParamDefinition> headers;
    private List<ParamDefinition> body;

}
