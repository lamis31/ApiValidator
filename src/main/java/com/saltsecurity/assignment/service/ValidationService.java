package com.saltsecurity.assignment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.saltsecurity.assignment.model.Anomaly;
import com.saltsecurity.assignment.model.ApiModel;
import com.saltsecurity.assignment.model.RequestData;
import com.saltsecurity.assignment.model.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ModelStorageService modelStorageService;
    
    // Status constants
    private static final String STATUS_VALID = "valid";
    private static final String STATUS_ABNORMAL = "abnormal";
    
    // Parameter location constants
    private static final String LOCATION_QUERY = "query_params";
    private static final String LOCATION_HEADERS = "headers";
    private static final String LOCATION_BODY = "body";
    private static final String LOCATION_REQUEST = "request";

    /**
     * Validates an incoming request against the stored API model.
     *
     * @param requestData The incoming request data.
     * @return A ValidationResult indicating if the request is "valid" or
     *         "abnormal", including a list of anomalies if abnormal.
     */
    public ValidationResult validateRequest(RequestData requestData) {
        if (isRequestDataNotValid(requestData)) {
            return createAbnormalResult(
                new Anomaly(LOCATION_REQUEST, "Invalid request data (null or missing path/method)")
            );
        }

        Optional<ApiModel> modelOpt = modelStorageService.findModel(requestData.getMethod(), requestData.getPath());

        if (modelOpt.isEmpty()) {
            //return abnormal when no model is found
            log.warn("No API model found for {}:{}", requestData.getMethod(), requestData.getPath());
            return createAbnormalResult(
                new Anomaly(LOCATION_REQUEST, "No API model defined for this endpoint")
            );
        }

        ApiModel model = modelOpt.get();
        List<Anomaly> anomalies = new ArrayList<>();

        // Validate query parameters
        anomalies.addAll(ParameterValidator.validateParameterGroup(
            model.getQuery_params(), requestData.getQuery_params(), LOCATION_QUERY));

        // Validate headers
        anomalies.addAll(ParameterValidator.validateParameterGroup(
            model.getHeaders(), requestData.getHeaders(), LOCATION_HEADERS));

        // Validate body parameters
        anomalies.addAll(ParameterValidator.validateParameterGroup(
            model.getBody(), requestData.getBody(), LOCATION_BODY));

        return anomalies.isEmpty() ? createValidResult() : createAbnormalResult(anomalies);
    }

    private boolean isRequestDataNotValid(RequestData requestData) {
        return requestData == null || requestData.getMethod() == null || requestData.getPath() == null;
    }
    
    private ValidationResult createValidResult() {
        return new ValidationResult(STATUS_VALID, Collections.emptyList());
    }
    
    private ValidationResult createAbnormalResult(Anomaly anomaly) {
        return new ValidationResult(STATUS_ABNORMAL, List.of(anomaly));
    }
    
    private ValidationResult createAbnormalResult(List<Anomaly> anomalies) {
        return new ValidationResult(STATUS_ABNORMAL, anomalies);
    }
}
