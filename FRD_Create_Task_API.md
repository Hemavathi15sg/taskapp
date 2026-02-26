# Functional Requirements Document (FRD)
## Create Task API

**Document Version:** 1.0  
**Date:** February 26, 2026  
**Project:** Techwave (TEC)  
**Issue ID:** TEC-32  

---

## 1. API Endpoint Overview

| Property | Value |
|---|---|
| **HTTP Method** | POST |
| **Endpoint** | `/api/tasks` |
| **Content-Type** | `application/json` |
| **Authentication** | Required (Bearer token or credentials) |
| **Rate Limiting** | To be defined by infrastructure team |

---

## 2. Request Format

### 2.1 Request Headers
```
Content-Type: application/json
Authorization: Bearer <token>
```

### 2.2 Request Body Schema
```json
{
  "title": "string",
  "description": "string (optional)"
}
```

### 2.3 Request Body Field Definitions

| Field | Type | Required | Constraints | Example |
|---|---|---|---|---|
| `title` | String | Yes | Non-empty, max 255 characters | "Complete project documentation" |
| `description` | String | No | Max 2000 characters, can be null | "Write comprehensive API docs with examples" |

### 2.4 Request Examples

**Minimal Request (title only):**
```json
{
  "title": "Review code changes"
}
```

**Complete Request (with description):**
```json
{
  "title": "Setup database migrations",
  "description": "Create migration scripts for user authentication schema and run initial seeding"
}
```

---

## 3. Response Format

### 3.1 Success Response (HTTP 201 Created)

**Response Headers:**
```
Content-Type: application/json
Location: /api/tasks/{id}
```

**Response Body Schema:**
```json
{
  "id": "string",
  "title": "string",
  "description": "string or null",
  "status": "PENDING",
  "createdAt": "ISO 8601 timestamp"
}
```

### 3.2 Response Body Field Definitions

| Field | Type | Description | Example |
|---|---|---|---|
| `id` | String | Unique auto-generated task identifier | "550e8400-e29b-41d4-a716-446655440000" |
| `title` | String | The task title (echoed from request) | "Review code changes" |
| `description` | String \| null | The task description or null if not provided | "Review all pull requests from the sprint" |
| `status` | String | Always "PENDING" for newly created tasks | "PENDING" |
| `createdAt` | ISO 8601 Timestamp | UTC timestamp of task creation | "2026-02-26T12:00:15.246Z" |

### 3.3 Success Response Examples

**Minimal Task Created:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Review code changes",
  "description": null,
  "status": "PENDING",
  "createdAt": "2026-02-26T12:00:15.246Z"
}
```

**Complete Task Created:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Setup database migrations",
  "description": "Create migration scripts for user authentication schema and run initial seeding",
  "status": "PENDING",
  "createdAt": "2026-02-26T12:05:30.512Z"
}
```

---

## 4. Validation Rules

### 4.1 Title Validation
- **Mandatory**: Request must include `title` field
- **Non-empty**: Title cannot be an empty string, null, or contain only whitespace
- **Max Length**: 255 characters maximum
- **Trimming**: Leading/trailing whitespace should be trimmed
- **Allowed Characters**: All UTF-8 characters (alphanumeric, special characters, unicode)
- **Error**: Title validation failure returns HTTP 400

### 4.2 Description Validation
- **Optional**: Description field can be omitted from request
- **Nullable**: Can be explicitly set to null
- **Max Length**: 2000 characters maximum
- **Trimming**: Leading/trailing whitespace should be trimmed
- **Allowed Characters**: All UTF-8 characters
- **Empty Handling**: Empty string treated as null in response
- **Error**: Description exceeding max length returns HTTP 400

### 4.3 JSON Structure Validation
- Request body must be valid JSON
- Only `title` and `description` fields are accepted
- Unknown fields in request can be ignored or rejected (define behavior)
- Request body must not be empty

---

## 5. Error Scenarios

### 5.1 HTTP 400 Bad Request - Missing Title
**Scenario:** Request does not include the `title` field

**Request:**
```json
{
  "description": "Task without title"
}
```

**Response:**
```json
{
  "error": "Bad Request",
  "code": "MISSING_TITLE",
  "message": "Title field is required",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.2 HTTP 400 Bad Request - Empty/Whitespace Title
**Scenario:** Request includes title but it's empty or only whitespace

**Request:**
```json
{
  "title": "   ",
  "description": "Task with empty title"
}
```

**Response:**
```json
{
  "error": "Bad Request",
  "code": "INVALID_TITLE",
  "message": "Title cannot be empty or contain only whitespace",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.3 HTTP 400 Bad Request - Title Too Long
**Scenario:** Title exceeds maximum length of 255 characters

**Request:**
```json
{
  "title": "This is an extremely long title that exceeds the maximum allowed length of two hundred and fifty five characters. It goes on and on and on with unnecessary details that make the title far too long to be practical or useful in any real world scenario. This is definitely over the limit.",
  "description": "Task with overly long title"
}
```

**Response:**
```json
{
  "error": "Bad Request",
  "code": "TITLE_TOO_LONG",
  "message": "Title cannot exceed 255 characters",
  "details": {
    "max_length": 255,
    "provided_length": 342
  },
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.4 HTTP 400 Bad Request - Description Too Long
**Scenario:** Description exceeds maximum length of 2000 characters

**Response:**
```json
{
  "error": "Bad Request",
  "code": "DESCRIPTION_TOO_LONG",
  "message": "Description cannot exceed 2000 characters",
  "details": {
    "max_length": 2000,
    "provided_length": 2150
  },
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.5 HTTP 400 Bad Request - Invalid JSON
**Scenario:** Request body contains malformed JSON

**Request:**
```
{
  "title": "Invalid JSON,
  "description": "Missing closing brace"
```

**Response:**
```json
{
  "error": "Bad Request",
  "code": "INVALID_JSON",
  "message": "Request body must be valid JSON",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.6 HTTP 401 Unauthorized
**Scenario:** Missing or invalid authentication credentials

**Response:**
```json
{
  "error": "Unauthorized",
  "code": "MISSING_AUTH",
  "message": "Authentication credentials are required",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.7 HTTP 403 Forbidden
**Scenario:** Authenticated user lacks permission to create tasks

**Response:**
```json
{
  "error": "Forbidden",
  "code": "INSUFFICIENT_PERMISSIONS",
  "message": "You do not have permission to create tasks",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.8 HTTP 429 Too Many Requests
**Scenario:** Rate limit exceeded

**Response:**
```json
{
  "error": "Too Many Requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Please try again later.",
  "details": {
    "retry_after_seconds": 60
  },
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

### 5.9 HTTP 500 Internal Server Error
**Scenario:** Database or server error during task creation

**Response:**
```json
{
  "error": "Internal Server Error",
  "code": "SERVER_ERROR",
  "message": "An unexpected error occurred while creating the task",
  "request_id": "req-abc123def456",
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

**Note:** Do not expose sensitive error details to client. Log full error server-side with request_id for debugging.

---

### 5.10 HTTP 503 Service Unavailable
**Scenario:** Database or critical service temporarily unavailable

**Response:**
```json
{
  "error": "Service Unavailable",
  "code": "SERVICE_UNAVAILABLE",
  "message": "Service is temporarily unavailable. Please try again later.",
  "retry_after_seconds": 120,
  "timestamp": "2026-02-26T12:00:15.246Z"
}
```

---

## 6. Edge Cases & Special Handling

### 6.1 Unicode & Special Characters
- Title and description support full UTF-8 character set including emojis
- Example: `"title": "🚀 Deploy new feature to production"`

### 6.2 Null vs. Omitted Description
- If `description` is omitted from request: response returns `"description": null`
- If `description` is explicitly set to null: response returns `"description": null`
- If `description` is empty string: should be treated as null

### 6.3 Whitespace Handling
- Leading/trailing whitespace in title and description should be trimmed
- Internal whitespace should be preserved

### 6.4 Case Sensitivity
- API endpoint and field names are case-sensitive
- Status value "PENDING" is always uppercase

---

## 7. API Versioning

- **Current Version**: v1
- **Endpoint**: `/api/v1/tasks` (recommended for future compatibility)
- **Backward Compatibility**: To be defined in future versions

---

## 8. Performance Requirements

- Response time: < 500ms for successful requests
- Database: Support concurrent task creation
- Logging: All requests/responses should be logged asynchronously

---

## 9. Security Requirements

- All requests must be over HTTPS
- Input sanitization required to prevent injection attacks
- Rate limiting to prevent abuse
- CORS configuration as per infrastructure requirements

---

## 10. Testing Scenarios

### 10.1 Positive Test Cases
- [ ] Create task with title only
- [ ] Create task with title and description
- [ ] Create multiple tasks sequentially
- [ ] Verify each task receives unique ID
- [ ] Verify status is always "PENDING"
- [ ] Verify createdAt timestamp is accurate

### 10.2 Negative Test Cases
- [ ] Missing title field
- [ ] Empty title
- [ ] Whitespace-only title
- [ ] Title exceeding 255 characters
- [ ] Description exceeding 2000 characters
- [ ] Invalid JSON in request body
- [ ] Malformed Content-Type header
- [ ] Missing authentication

### 10.3 Integration Test Cases
- [ ] Verify task is retrievable after creation
- [ ] Verify task is persisted in database
- [ ] Verify concurrent task creation
- [ ] Verify transaction rollback on failure
