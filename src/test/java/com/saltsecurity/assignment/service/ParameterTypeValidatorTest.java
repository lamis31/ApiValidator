package com.saltsecurity.assignment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ParameterTypeValidatorTest {

    @Test
    void isValidType_nullOrEmptyTypes_shouldReturnTrue() {
        assertTrue(ParameterTypeValidator.isValidType("anyValue", null));
        assertTrue(ParameterTypeValidator.isValidType("anyValue", Collections.emptyList()));
    }

    @Test
    void isValidType_nullType_shouldSkipTypeAndContinue() {
        // If a type in the list is null, it should be skipped
        List<String> types = Arrays.asList(null, "string");
        assertTrue(ParameterTypeValidator.isValidType("test", types));
    }

    @ParameterizedTest
    @MethodSource("validTypeTestCases")
    void isValidType_validValues_shouldReturnTrue(String value, List<String> types) {
        assertTrue(ParameterTypeValidator.isValidType(value, types));
    }

    @ParameterizedTest
    @MethodSource("invalidTypeTestCases")
    void isValidType_invalidValues_shouldReturnFalse(String value, List<String> types) {
        assertFalse(ParameterTypeValidator.isValidType(value, types));
    }

    // Test cases for valid type combinations
    static Stream<Arguments> validTypeTestCases() {
        return Stream.of(
            // String type accepts any non-null value
            Arguments.of("anyString", Collections.singletonList("string")),
            
            // Integer type tests
            Arguments.of("123", Collections.singletonList("int")),
            Arguments.of("0", Collections.singletonList("int")),
            Arguments.of("-123", Collections.singletonList("int")),
            
            // Boolean type tests
            Arguments.of("true", Collections.singletonList("boolean")),
            Arguments.of("false", Collections.singletonList("boolean")),
            Arguments.of("TRUE", Collections.singletonList("boolean")),
            Arguments.of("FALSE", Collections.singletonList("boolean")),
            
            // List type tests
            Arguments.of("[]", Collections.singletonList("list")),
            Arguments.of("[1,2,3]", Collections.singletonList("list")),
            Arguments.of("[\"a\",\"b\"]", Collections.singletonList("list")),
            
            // Date type tests
            Arguments.of("01-01-2023", Collections.singletonList("date")),
            Arguments.of("31-12-2023", Collections.singletonList("date")),
            
            // Email type tests
            Arguments.of("test@example.com", Collections.singletonList("email")),
            Arguments.of("user.name@domain.co.uk", Collections.singletonList("email")),
            
            // UUID type tests
            Arguments.of("123e4567-e89b-12d3-a456-426614174000", Collections.singletonList("uuid")),
            
            // Auth token tests
            Arguments.of("Bearer abcdef123456", Collections.singletonList("auth-token")),
            
            // Multiple types (should pass if value matches at least one type)
            Arguments.of("123", Arrays.asList("string", "int")),
            Arguments.of("not-a-number", Arrays.asList("string", "int"))
        );
    }

    // Test cases for invalid type combinations
    static Stream<Arguments> invalidTypeTestCases() {
        return Stream.of(
            // Integer type invalid tests
            Arguments.of("not-a-number", Collections.singletonList("int")),
            Arguments.of("123.45", Collections.singletonList("int")),
            
            // Boolean type invalid tests
            Arguments.of("yes", Collections.singletonList("boolean")),
            Arguments.of("1", Collections.singletonList("boolean")),
            
            // List type invalid tests
            Arguments.of("not-a-list", Collections.singletonList("list")),
            Arguments.of("{object}", Collections.singletonList("list")),
            
            // Date type invalid tests
            Arguments.of("2023-01-01", Collections.singletonList("date")), // wrong format
            Arguments.of("32-13-2023", Collections.singletonList("date")), // invalid date
            
            // Email type invalid tests
            Arguments.of("not-an-email", Collections.singletonList("email")),
            Arguments.of("missing-at-domain.com", Collections.singletonList("email")),
            
            // UUID type invalid tests
            Arguments.of("not-a-uuid", Collections.singletonList("uuid")),
            Arguments.of("123e4567-e89b-12d3-a456-42661417400", Collections.singletonList("uuid")), // too short
            
            // Auth token invalid tests
            Arguments.of("BearerWithoutSpace123456", Collections.singletonList("auth-token")),
            Arguments.of("NotBearer 123456", Collections.singletonList("auth-token")),
            
            // Unknown type
            Arguments.of("any-value", Collections.singletonList("unknown-type"))
        );
    }

    @Test
    void paramType_fromString_shouldReturnCorrectEnum() {
        // Test all enum values are correctly mapped from their string representation
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.STRING), 
                    ParameterTypeValidator.ParamType.fromString("string"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.INT), 
                    ParameterTypeValidator.ParamType.fromString("int"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.BOOLEAN), 
                    ParameterTypeValidator.ParamType.fromString("boolean"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.LIST), 
                    ParameterTypeValidator.ParamType.fromString("list"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.DATE), 
                    ParameterTypeValidator.ParamType.fromString("date"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.EMAIL), 
                    ParameterTypeValidator.ParamType.fromString("email"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.UUID), 
                    ParameterTypeValidator.ParamType.fromString("uuid"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.AUTH_TOKEN), 
                    ParameterTypeValidator.ParamType.fromString("auth-token"));
    }

    @Test
    void paramType_fromString_shouldBeCaseInsensitive() {
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.STRING), 
                    ParameterTypeValidator.ParamType.fromString("STRING"));
        assertEquals(Optional.of(ParameterTypeValidator.ParamType.INT), 
                    ParameterTypeValidator.ParamType.fromString("Int"));
    }

    @Test
    void paramType_fromString_shouldHandleNullAndUnknownTypes() {
        assertEquals(Optional.empty(), ParameterTypeValidator.ParamType.fromString(null));
        assertEquals(Optional.empty(), ParameterTypeValidator.ParamType.fromString("unknown-type"));
    }
} 