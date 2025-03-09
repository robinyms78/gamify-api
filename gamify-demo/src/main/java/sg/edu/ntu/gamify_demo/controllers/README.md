# Authentication API Documentation

This document provides information about the authentication endpoints available in the Gamify API.

## Endpoints

### User Registration

Registers a new user in the system.

- **URL**: `/auth/register`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "string",
    "email": "string",
    "password": "string",
    "role": "EMPLOYEE | MANAGER | ADMIN",
    "department": "string"
  }
  ```
- **Success Response**:
  - **Code**: 201 CREATED
  - **Content**:
    ```json
    {
      "message": "User registered successfully",
      "userId": "string"
    }
    ```
- **Error Responses**:
  - **Code**: 409 CONFLICT
  - **Content**:
    ```json
    {
      "error": "Registration failed",
      "message": "Username already exists"
    }
    ```
  - **Code**: 409 CONFLICT
  - **Content**:
    ```json
    {
      "error": "Registration failed",
      "message": "Email already exists"
    }
    ```
  - **Code**: 400 BAD REQUEST
  - **Content**:
    ```json
    {
      "error": "Validation failed",
      "message": "Error message details"
    }
    ```

### User Login

Authenticates a user and returns a JWT token.

- **URL**: `/auth/login`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Success Response**:
  - **Code**: 200 OK
  - **Content**:
    ```json
    {
      "token": "JWT token string",
      "user": {
        "id": "string",
        "username": "string",
        "email": "string",
        "role": "EMPLOYEE | MANAGER | ADMIN",
        "department": "string",
        "earnedPoints": 0,
        "availablePoints": 0,
        "createdAt": "timestamp",
        "updatedAt": "timestamp"
      }
    }
    ```
- **Error Response**:
  - **Code**: 401 UNAUTHORIZED
  - **Content**:
    ```json
    {
      "error": "Authentication failed",
      "message": "Invalid credentials"
    }
    ```

## Implementation Details

The authentication endpoints are implemented in the `AuthController` class. The controller uses:

1. `UserRepository` to check for existing users and save new users
2. `PasswordEncoder` to securely hash passwords
3. `AuthenticationService` to generate JWT tokens

When a new user is registered, their `earnedPoints` and `availablePoints` are initialized to 0, as specified in the user story.

## Error Handling

Authentication-related errors are handled by the `GlobalExceptionHandler` class, which provides appropriate HTTP status codes and error messages for different types of exceptions:

- `AuthenticationException`: 401 Unauthorized
- `DuplicateUserException`: 409 Conflict
- `UserValidationException`: 400 Bad Request
- `UserNotFoundException`: 404 Not Found
