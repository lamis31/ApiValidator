package com.saltsecurity.assignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestData {
    private String path;
    private String method;
    private List<ParamValue> query_params;
    private List<ParamValue> headers;
    private List<ParamValue> body;
}
