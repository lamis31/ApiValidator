package com.saltsecurity.assignment.service;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Helper class for validating different parameter types.
 */
public class ParameterTypeValidator {

    // Define patterns for validation (pre-compile for efficiency)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern UUID_PATTERN = Pattern
            .compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern AUTH_TOKEN_PATTERN = Pattern.compile("^Bearer [a-zA-Z0-9]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Enum for parameter types with validation logic
    public enum ParamType {
        STRING("string", val -> true), // Any string is valid
        INT("int", ParameterTypeValidator::isInteger),
        BOOLEAN("boolean", ParameterTypeValidator::isBoolean),
        LIST("list", ParameterTypeValidator::isListLike),
        DATE("date", ParameterTypeValidator::isDate),
        EMAIL("email", ParameterTypeValidator::isValidEmail),
        UUID("uuid", ParameterTypeValidator::isValidUuid),
        AUTH_TOKEN("auth-token", ParameterTypeValidator::isValidAuthToken);

        private final String typeName;
        private final Predicate<String> validator;

        ParamType(String typeName, Predicate<String> validator) {
            this.typeName = typeName;
            this.validator = validator;
        }

        public boolean isValid(String value) {
            return validator.test(value);
        }

        public static Optional<ParamType> fromString(String type) {
            if (type == null)
                return Optional.empty();

            String normalizedType = type.toLowerCase();
            for (ParamType paramType : values()) {
                if (paramType.typeName.equals(normalizedType)) {
                    return Optional.of(paramType);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Checks if the given value matches AT LEAST ONE of the expected types.
     *
     * @param value         The actual parameter value string.
     * @param expectedTypes The list of allowed type names (e.g., ["String", "Int",
     *                      "Email"]).
     * @return true if the value conforms to at least one expected type, false
     *         otherwise.
     */
    public static boolean isValidType(String value, List<String> expectedTypes) {
        if (expectedTypes == null || expectedTypes.isEmpty()) {
            return true; // No type defined, assuming valid.
        }

        for (String type : expectedTypes) {
            if (type == null) {
                continue;
            }

            Optional<ParamType> paramTypeOpt = ParamType.fromString(type);
            if (paramTypeOpt.isPresent() && paramTypeOpt.get().isValid(value)) {
                return true;
            }
        }

        // If none of the expected types matched
        return false;
    }

    // --- Type Validation Helper Methods ---

    private static boolean isInteger(String value) {
        if (value == null)
            return false;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static boolean isListLike(String value) {
        // Very basic check. Doesn't validate content or correct JSON syntax.
        return value != null && value.startsWith("[") && value.endsWith("]");
    }

    private static boolean isDate(String value) {
        if (value == null)
            return false;
        try {
            DATE_FORMATTER.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }

    private static boolean isValidUuid(String value) {
        return value != null && UUID_PATTERN.matcher(value).matches();
    }

    private static boolean isValidAuthToken(String value) {
        return value != null && AUTH_TOKEN_PATTERN.matcher(value).matches();
    }
}