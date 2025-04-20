# API Validator

A Spring Boot service that validates incoming API requests against predefined models to detect anomalies.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
  - [Key Components](#key-components)
- [Technology Stack](#technology-stack)
- [API Endpoints](#api-endpoints)
  - [Load Models](#load-models)
  - [Validate Request](#validate-request)
  - [Batch Validate Requests](#batch-validate-requests)
- [Validation Behavior](#validation-behavior)
- [Error Handling](#error-handling)
  - [JSON Parsing Errors](#json-parsing-errors)
  - [Server Errors](#server-errors)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
  - [Running the Application](#running-the-application)
- [Development](#development)
  - [Project Structure](#project-structure)
- [License](#license)

## Overview

This service can validate request parameters (query parameters, headers, and body elements) against defined API models to ensure they match expected types and requirements. It identifies incorrect parameter types, missing required parameters, and other anomalies.

## Features

- Load and store API models with parameter definitions
- Validate incoming requests against stored models
- Support for multiple parameter types:
  - Strings
  - Integers
  - Booleans
  - Lists
  - Dates (dd-MM-yyyy format)
  - Emails
  - UUIDs
  - Auth tokens
- Security features:
  - Detection of unexpected parameters not defined in API models
  - Proper anomaly reporting for undefined endpoints
  - Comprehensive input validation
- RESTful API endpoints for model loading and request validation
- Batch validation support for processing multiple requests in one call
- Detailed anomaly reporting
- Comprehensive error handling with descriptive messages
- Performance optimizations:
  - Method-based partitioning for faster lookups
  - In-memory caching of frequently accessed models
  - Efficient path normalization
  - Thread-safe concurrent implementation

## Architecture

The project follows a clean architecture approach with separation of concerns:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic for validation and model storage
- **Models**: Define data structures

### Key Components

- `ValidationController`: Exposes REST endpoints for loading models and validating requests
- `ValidationService`: Orchestrates the validation process
- `ModelStorageService`: Manages storage and retrieval of API models
- `ParameterValidator`: Validates request parameters against their definitions
- `ParameterTypeValidator`: Verifies parameter values match their expected types
- `GlobalExceptionHandler`: Provides consistent error responses for API exceptions

## Technology Stack

This project leverages a robust set of technologies to ensure reliability, performance, and maintainability:

### Core Technologies
- **Java 17**: Utilizes the latest LTS version with enhanced language features like records, pattern matching, and improved null handling
- **Spring Boot 3.x**: Provides the foundation for building production-grade applications with minimal configuration
- **Spring Web**: For building RESTful web services with annotation-based routing and request handling
- **Spring Test**: Comprehensive testing framework for unit and integration testing

### Development Tools
- **Gradle 8.x**: Modern build system with dependency management, task automation, and extensible build scripts
- **JUnit 5**: Next-generation testing framework with powerful features for parameterized tests and test lifecycle management
- **Mockito**: Mocking framework for creating test doubles in unit tests
- **Lombok**: Reduces boilerplate code through annotations for generating getters, setters, constructors, and more
- **SLF4J**: Simple Logging Facade for Java with multiple logging level support

### Architecture and Design Patterns
- **RESTful API**: Follows REST principles for scalable, stateless API design
- **Dependency Injection**: Utilizes Spring's IoC container for loose coupling and better testability
- **Thread Safety**: Implemented with ConcurrentHashMap and immutable model objects for reliable concurrent access
- **Service-Oriented Architecture**: Clean separation of concerns with controller, service, and model layers

### Runtime Efficiency
- **In-Memory Storage**: Fast model access with optimized data structures
- **Effective Caching**: Method-level caching for frequently accessed resources
- **Stream API**: Functional-style operations for efficient collection processing

### Code Quality and Testing
- **Comprehensive Test Suite**: Over 50 unit tests ensuring code reliability and regression prevention
- **Parameterized Testing**: Data-driven tests validating multiple scenarios efficiently
- **Test Coverage**: High test coverage focusing on critical business logic components

## API Endpoints

### Load Models

```
POST /api/models
```

Loads API models into the system for later validation.

**Request Body**: A JSON array of API model objects.

**Example**:
```json
[
  {
    "path": "/users",
    "method": "GET",
    "query_params": [
      {
        "name": "limit",
        "types": ["int"],
        "required": false
      }
    ],
    "headers": [
      {
        "name": "Authorization",
        "types": ["auth-token"],
        "required": true
      }
    ],
    "body": []
  }
]
```

### Validate Request

```
POST /api/validate
```

Validates an incoming request against previously loaded models.

**Request Body**: A JSON object describing the request to validate.

**Example**:
```json
{
  "path": "/users",
  "method": "GET",
  "query_params": [
    {
      "name": "limit",
      "value": "10"
    }
  ],
  "headers": [
    {
      "name": "Authorization",
      "value": "Bearer abc123"
    }
  ],
  "body": []
}
```

**Response**: A validation result indicating if the request is valid or has anomalies.

**Example**:
```json
{
  "status": "valid",
  "anomalies": []
}
```

Or with anomalies:
```json
{
  "status": "abnormal",
  "anomalies": [
    {
      "field": "query_params[limit]",
      "reason": "Type mismatch. Expected: [int], Actual Value: 'abc'"
    }
  ]
}
```

### Batch Validate Requests

```
POST /api/validate/batch
```

Validates multiple requests in a single API call.

**Request Body**: A JSON array of request objects to validate.

**Example**:
```json
[
  {
    "path": "/users",
    "method": "GET",
    "query_params": [
      {
        "name": "limit",
        "value": "10"
      }
    ],
    "headers": [],
    "body": []
  },
  {
    "path": "/orders",
    "method": "POST",
    "query_params": [],
    "headers": [
      {
        "name": "Authorization",
        "value": "Bearer xyz789"
      }
    ],
    "body": [
      {
        "name": "product_id",
        "value": "123"
      }
    ]
  }
]
```

**Response**: A JSON array of validation results in the same order as the requests.

**Example**:
```json
[
  {
    "status": "valid",
    "anomalies": []
  },
  {
    "status": "abnormal",
    "anomalies": [
      {
        "field": "body[product_id]",
        "reason": "Type mismatch. Expected: [uuid], Actual Value: '123'"
      }
    ]
  }
]
```

## Validation Behavior

The service implements these key validation behaviors:

1. **Undefined Endpoints**: Requests to endpoints with no matching API model are marked as "abnormal" with the reason "No API model defined for this endpoint".

2. **Path Normalization**: To improve usability and reduce configuration errors, paths are normalized:
   - Trailing slashes are removed (e.g., `/users/` becomes `/users`)
   - Missing leading slashes are added (e.g., `users` becomes `/users`)
   - Paths are case-insensitive (e.g., `/Users` and `/users` match the same model)
   - Root path (`/`) is preserved as-is

3. **Parameter Validation**: For defined endpoints, the service validates:
   - Required parameters are present
   - All parameters match their expected types
   - Values conform to their format specification (e.g., emails, UUIDs)
   - No unexpected parameters are sent that aren't defined in the API model

4. **Validation Result**: The response will have:
   - `status`: "valid" or "abnormal"
   - `anomalies`: List of validation issues with field name and reason

## Error Handling

The API provides consistent error responses for various error conditions:

### JSON Parsing Errors

If the request contains malformed JSON or doesn't match the expected structure:

```json
{
  "status": "error",
  "message": "Invalid JSON format or structure in request body",
  "details": "Detailed error message from the parser"
}
```

### Server Errors

For unexpected server errors:

```json
{
  "status": "error",
  "message": "An unexpected server error occurred",
  "details": "Error details"
}
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 7.6+ (or use the included Gradle wrapper)

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test      # Run default tests
./gradlew unitTest  # Run comprehensive unit tests with detailed output
```

### Running the Application

```bash
./gradlew bootRun
```

The service will start on port 8080 by default.

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/saltsecurity/assignment/
│   │       ├── controller/
│   │       │   ├── ValidationController.java
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── model/
│   │       ├── service/
│   │       └── AssignmentApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/saltsecurity/assignment/
            ├── controller/
            ├── service/
            └── AssignmentApplicationTests.java
```

## License

This project is proprietary and confidential. 