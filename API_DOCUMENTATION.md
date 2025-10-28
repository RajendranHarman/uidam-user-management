# UIDAM User Management API Documentation

This document provides detailed API documentation for the UIDAM User Management microservice. APIs are grouped by functionality and follow a uniform CRUD order within each group. Each API section includes a description, details table, payload definitions, response details, and a PlanUML sequence flow diagram.

---

## Table of Contents
- [User Management](#user-management)
- [Role Management](#role-management)
- [Scope Management](#scope-management)
- [OAuth2 Client Management](#oauth2-client-management)

---

## User Management

### 1. Create User
**API Description:**
Creates a new user in the system. Triggers email verification. Handles both internal and self-registration flows.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/users` |
| Method             | POST |
| Allowed scopes     | `ManageUsers` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | None |
| Payload            | `userDto` (object) |
| Request attribute definition | See below |
| Response Payload   | `UserResponseV1` |
| Response status    | 201 - Created, 400 - Bad Request |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| userName  | String | Yes | Unique username |
| email     | String | Yes | User email address |
| password  | String | Yes | Password (policy enforced) |
| ...       | ...    | ...       | ...         |

**Response:**
| Attribute | Type | Description |
|-----------|------|-------------|
| id        | BigInteger | User ID |
| userName  | String | Username |
| email     | String | Email |
| ...       | ...    | ...         |

**Response Status Codes:**
- 201: Created
- 400: Bad Request

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
participant "Notification Center" as NC
User -> APIGW: POST /v1/users
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request payload
UMS -> UMS: Check if request user (userId in header) has required scopes to create user
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> UMS: Check for duplicate user
alt Duplicate User
UMS -> APIGW: 409 Conflict
APIGW -> User: Response
else Not Duplicate
UMS -> DB: Create user (tenant-specific)
alt User Lifecycle Enabled
UMS -> UMS: Trigger lifecycle hooks
end
alt Email Verification Enabled
UMS -> NC: Send verification email
end
UMS -> APIGW: 201 Created
APIGW -> User: Response
end
end
@enduml
```

---

### 2. Get User
**API Description:**
Fetches user details by user ID.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/users/{id}` |
| Method             | GET |
| Allowed scopes     | `ViewUsers`, `ManageUsers` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | `id` (path) |
| Payload            | None |
| Request attribute definition | N/A |
| Response Payload   | `UserResponseV1` |
| Response status    | 200 - OK, 404 - Not Found |

**Response Status Codes:**
- 200: OK
- 404: Not Found

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: GET /v1/users/{id}
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes for view
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Fetch user (tenant-specific)
alt User Found
UMS -> APIGW: 200 OK
else User Not Found
UMS -> APIGW: 404 Not Found
end
APIGW -> User: Response
end
@enduml
```

---

### 3. Get External User
**API Description:**
Fetches details of an external user by user ID.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/users/external/{id}` |
| Method             | GET |
| Allowed scopes     | `ViewUsers`, `ManageUsers` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | `id` (path) |
| Payload            | None |
| Request attribute definition | N/A |
| Response Payload   | `UserResponseV1` |
| Response status    | 200 - OK, 404 - Not Found |

**Response Status Codes:**
- 200: OK
- 404: Not Found

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: GET /v1/users/external/{id}
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes for view
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Fetch external user (tenant-specific)
alt User Found
UMS -> APIGW: 200 OK
else User Not Found
UMS -> APIGW: 404 Not Found
end
APIGW -> User: Response
end
@enduml
```

---

### 4. Get User By Username
**API Description:**
Fetches user details by username and optional account name.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/users/{username}/by-username` |
| Method             | GET |
| Allowed scopes     | (Internal) |
| Request Headers    | `accountName` (optional) |
| Request Parameters | `username` (path) |
| Payload            | None |
| Request attribute definition | N/A |
| Response Payload   | `UserDetailsResponse` |
| Response status    | 200 - OK, 404 - Not Found, 403 - Inactive User |

**Response Status Codes:**
- 200: OK
- 404: Not Found
- 403: Inactive User

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: GET /v1/users/{username}/by-username
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> DB: Fetch user by username (tenant-specific)
alt User Found and Active
UMS -> APIGW: 200 OK
else User Not Found
UMS -> APIGW: 404 Not Found
else User Inactive
UMS -> APIGW: 403 Inactive User
end
APIGW -> User: Response
end
@enduml
```

---

## Role Management

### 1. Create Role
**API Description:**
Creates a new role with associated scopes.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles` |
| Method             | POST |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `userId` (mandatory) |
| Request Parameters | None |
| Payload            | `RolesCreateRequestDto` |
| Request attribute definition | See below |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 201 - Created, 400 - Bad Request |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| name      | String | Yes | Role name |
| scopeNames| List<String> | Yes | Associated scopes |
| ...       | ...    | ...       | ...         |

**Response Status Codes:**
- 201: Created
- 400: Bad Request

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: POST /v1/roles
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request payload
UMS -> UMS: Check if request user has required scopes to create role
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> UMS: Check for duplicate role
alt Duplicate Role
UMS -> APIGW: 409 Conflict
APIGW -> User: Response
else Not Duplicate
UMS -> DB: Create role (tenant-specific)
UMS -> APIGW: 201 Created
APIGW -> User: Response
end
end
@enduml
```

---

### 2. Get Role By Name
**API Description:**
Fetches role details by role name.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles/{name}` |
| Method             | GET |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | `name` (path) |
| Payload            | None |
| Request attribute definition | N/A |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 200 - OK, 404 - Not Found |

**Response Status Codes:**
- 200: OK
- 404: Not Found

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: GET /v1/roles/{name}
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Fetch role by name (tenant-specific)
alt Role Found
UMS -> APIGW: 200 OK
else Role Not Found
UMS -> APIGW: 404 Not Found
end
APIGW -> User: Response
end
@enduml
```

---

### 3. Get Role By Id
**API Description:**
Fetches role details by role ID(s).

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles/by-id` |
| Method             | POST |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | None |
| Payload            | `RoleIdRequest` |
| Request attribute definition | See below |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 200 - OK, 404 - Not Found |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| roleId    | List<BigInteger> | Yes | List of role IDs |

**Response Status Codes:**
- 200: OK
- 404: Not Found

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: POST /v1/roles/by-id
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Fetch roles by ID (tenant-specific)
alt Roles Found
UMS -> APIGW: 200 OK
else Roles Not Found
UMS -> APIGW: 404 Not Found
end
APIGW -> User: Response
end
@enduml
```

---

### 4. Filter Roles
**API Description:**
Fetches multiple roles by filter criteria.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles/filter` |
| Method             | POST |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | `page`, `pageSize` (query) |
| Payload            | `RolesFilterRequest` |
| Request attribute definition | See below |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 200 - OK |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| roles     | List<String> | Yes | List of role names to filter |

**Response Status Codes:**
- 200: OK

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: POST /v1/roles/filter
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Filter roles (tenant-specific)
UMS -> APIGW: 200 OK
APIGW -> User: Response
end
@enduml
```

---

### 5. Update Role
**API Description:**
Updates an existing role by name.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles/{name}` |
| Method             | PATCH |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `userId` (mandatory) |
| Request Parameters | `name` (path) |
| Payload            | `RolePatch` |
| Request attribute definition | See below |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 200 - OK, 404 - Not Found |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| ...       | ...    | ...       | ...         |

**Response Status Codes:**
- 200: OK
- 404: Not Found

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: PATCH /v1/roles/{name}
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Update role (tenant-specific)
alt Role Updated
UMS -> APIGW: 200 OK
else Role Not Found
UMS -> APIGW: 404 Not Found
end
APIGW -> User: Response
end
@enduml
```

---

### 6. Delete Role
**API Description:**
Deletes a role by name.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/roles/{name}` |
| Method             | DELETE |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `userId` (mandatory) |
| Request Parameters | `name` (path) |
| Payload            | None |
| Request attribute definition | N/A |
| Response Payload   | `RoleListRepresentation` |
| Response status    | 200 - OK, 404 - Not Found, 400 - Bad Request |

**Response Status Codes:**
- 200: OK
- 404: Not Found
- 400: Bad Request

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: DELETE /v1/roles/{name}
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request user scopes
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> DB: Delete role (tenant-specific)
alt Role Deleted
UMS -> APIGW: 200 OK
else Role Not Found
UMS -> APIGW: 404 Not Found
else Bad Request
UMS -> APIGW: 400 Bad Request
end
APIGW -> User: Response
end
@enduml
```

---

## Scope Management

### 1. Create Scope
**API Description:**
Creates a new scope for role-based access control.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/scopes` |
| Method             | POST |
| Allowed scopes     | `ManageUserRolesAndPermissions` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | None |
| Payload            | `ScopeDto` |
| Request attribute definition | See below |
| Response Payload   | `ScopeListRepresentation` |
| Response status    | 201 - Created, 401 - Unauthorized |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| name      | String | Yes | Scope name |
| ...       | ...    | ...       | ...         |

**Response Status Codes:**
- 201: Created
- 401: Unauthorized

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: POST /v1/scopes
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request payload
UMS -> UMS: Check if request user has required scopes to create scope
alt Insufficient Privileges
UMS -> APIGW: 401 Unauthorized
APIGW -> User: Response
else Sufficient Privileges
UMS -> UMS: Check for duplicate scope
alt Duplicate Scope
UMS -> APIGW: 409 Conflict
APIGW -> User: Response
else Not Duplicate
UMS -> DB: Create scope (tenant-specific)
UMS -> APIGW: 201 Created
APIGW -> User: Response
end
end
@enduml
```

---

## OAuth2 Client Management

### 1. Create Client
**API Description:**
Registers a new OAuth2 client for authentication and authorization.

| Key                | Value |
|--------------------|-------|
| End-point          | `/v1/clients` |
| Method             | POST |
| Allowed scopes     | `OAuth2ClientMgmt` |
| Request Headers    | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters | None |
| Payload            | `RegisteredClientDetails` |
| Request attribute definition | See below |
| Response Payload   | `BaseResponse` |
| Response status    | 201 - Created |

**Payload Attributes:**
| Attribute | Type | Mandatory | Description |
|-----------|------|-----------|-------------|
| clientName| String | Yes | Client name |
| ...       | ...    | ...       | ...         |

**Response Status Codes:**
- 201: Created

**PlanUML Sequence Flow:**
```
@startuml
actor User
participant "API Gateway" as APIGW
participant "User Mgmt MS" as UMS
participant "PostgresDB" as DB
User -> APIGW: POST /v1/clients
APIGW -> UMS: Forward request
UMS -> UMS: Validate tenant, update tenantContext
UMS -> UMS: Validate request payload
UMS -> UMS: Check if request user has required scopes to create client
alt Insufficient Privileges
UMS -> APIGW: 403 Forbidden
APIGW -> User: Response
else Sufficient Privileges
UMS -> UMS: Check for duplicate client
alt Duplicate Client
UMS -> APIGW: 409 Conflict
APIGW -> User: Response
else Not Duplicate
UMS -> DB: Register client (tenant-specific)
UMS -> APIGW: 201 Created
APIGW -> User: Response
end
end
@enduml
```

---

## Notes
- All APIs implicitly resolve tenantId from tenantContext for database operations.
- Default headers: `tenantId`, `correlationId`, `scopes` (optional unless used for validation).
- Error scenarios and alternate flows are covered in PlanUML diagrams.
- For full attribute definitions, refer to DTO classes in the codebase.

---

*This documentation is auto-generated and should be reviewed for completeness and accuracy.*
