# Roles Management API Documentation

## 1. Create Role

### API Description
Creates a new role in the UIDAM system. Validates role name, description, and permissions. Checks for uniqueness and required fields. Handles exceptions for invalid data, duplicate role, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles` |
| Method                | POST |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | None |
| Payload               | `RolesCreateRequestDto` |
| Request Attribute Definition | See below |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>201 - Created: Role created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: Role already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **name**: String (required, unique, validated)
- **description**: String (required, max 255 chars)
- **scopeNames**: Set<String> (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| name | Required. Unique role name. Must match pattern and length. |
| description | Required. Role description. |
| scopeNames | Optional. Set of scope names. |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of created role objects |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/roles (payload, headers)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> RoleMgmtMS: Validate payload fields
RoleMgmtMS -> PostgresDB: Check for duplicate role
alt Role Exists
PostgresDB -> RoleMgmtMS: Role found
RoleMgmtMS -> api-gateway: 409 Conflict
api-gateway -> AdminUser: 409 Conflict
else Role Not Exists
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> PostgresDB: Save role entity
RoleMgmtMS -> api-gateway: 201 Created (role details)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 2. Get Role By Name

### API Description
Retrieves a role by its unique name. Validates the name, checks existence, and returns details. Handles exceptions for not found or invalid name.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/{name}` |
| Method                | GET |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | None |
| Request Parameters    | `name` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `name`: Required. Role name. |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Role found</li><li>404 - Not Found: Role does not exist</li><li>400 - Bad Request: Invalid role name</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of role objects matching name |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: GET /v1/roles/{name}
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> PostgresDB: Find role by name
alt Role Found
PostgresDB -> RoleMgmtMS: Role entity
RoleMgmtMS -> api-gateway: 200 OK (role details)
api-gateway -> AdminUser: 200 OK
else Role Not Found
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid Name
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 3. Get Role By Id

### API Description
Retrieves one or more roles by their unique IDs. Validates IDs, checks existence, and returns details. Handles exceptions for not found or invalid IDs.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/by-id` |
| Method                | POST |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | None |
| Request Parameters    | None |
| Payload               | `RoleIdRequest` (list of role IDs) |
| Request Attribute Definition | `roleId`: Required. List of role IDs. |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Roles found</li><li>404 - Not Found: Role(s) do not exist</li><li>400 - Bad Request: Invalid role ID(s)</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **roleId**: List<BigInteger> (required)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roleId | Required. List of role unique identifiers. |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of role objects matching IDs |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/roles/by-id (payload)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> PostgresDB: Find roles by IDs
alt Roles Found
PostgresDB -> RoleMgmtMS: Role entities
RoleMgmtMS -> api-gateway: 200 OK (role details)
api-gateway -> AdminUser: 200 OK
else Roles Not Found
PostgresDB -> RoleMgmtMS: No roles found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid IDs
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 4. Filter Roles

### API Description
Filters roles based on criteria such as name, description, permissions, and pagination. Validates filter object, applies query, and returns paginated list. Handles exceptions for invalid filter, no results, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/filter` |
| Method                | POST |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | None |
| Request Parameters    | `page` (query, optional), `pageSize` (query, optional) |
| Payload               | `RolesFilterRequest` |
| Request Attribute Definition | See below |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Roles found</li><li>400 - Bad Request: Invalid filter</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **roles**: List<String> (optional)
- **description**: String (optional)
- **scopeNames**: Set<String> (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | Filter by role names |
| description | Filter by description |
| scopeNames | Filter by scope names |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>
- **totalCount**: Integer
- **page**: Integer
- **size**: Integer

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of role objects matching filter |
| totalCount | Total number of roles matching filter |
| page | Current page number |
| size | Page size |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/roles/filter (payload, query params)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> PostgresDB: Query roles by filter
PostgresDB -> RoleMgmtMS: Paginated role list
RoleMgmtMS -> api-gateway: 200 OK (roles, count, page, size)
api-gateway -> AdminUser: 200 OK
else Invalid Filter
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 5. Update Role

### API Description
Updates details of a role identified by its unique name. Validates input, checks permissions, and updates allowed fields. Handles exceptions for invalid data, not found roles, and persistence errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/{name}` |
| Method                | PATCH |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | `name` (path, required, String) |
| Payload               | `RolePatch` |
| Request Attribute Definition | See below |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Role updated</li><li>400 - Bad Request: Validation failed</li><li>404 - Not Found: Role does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **description**: String (optional, max 255 chars)
- **scopeNames**: Set<String> (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| description | Optional. Role description. |
| scopeNames | Optional. Set of scope names. |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of updated role objects |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PATCH /v1/roles/{name} (payload, headers)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> PostgresDB: Find role by name
alt Role Found
PostgresDB -> RoleMgmtMS: Role entity
RoleMgmtMS -> PostgresDB: Save updated role
RoleMgmtMS -> api-gateway: 200 OK (updated role)
api-gateway -> AdminUser: 200 OK
else Role Not Found
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Validation Error
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 6. Delete Role

### API Description
Deletes a role identified by its unique name. Validates the name, checks permissions, and marks the role as deleted. Handles exceptions for not found roles, invalid name, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/{name}` |
| Method                | DELETE |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | `name` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `name`: Required. Role name. |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Role deleted</li><li>404 - Not Found: Role does not exist</li><li>400 - Bad Request: Invalid role name</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of roles after deletion |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/roles/{name} (headers)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> PostgresDB: Find role by name
alt Role Found
PostgresDB -> RoleMgmtMS: Role entity
RoleMgmtMS -> PostgresDB: Mark role as deleted
RoleMgmtMS -> api-gateway: 200 OK (roles)
api-gateway -> AdminUser: 200 OK
else Role Not Found
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid Name
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

# Scopes Management API Documentation

## 1. Create Scope

### API Description
Creates a new scope in the UIDAM system. Validates scope name, description, and uniqueness. Handles exceptions for invalid data, duplicate scope, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/scopes` |
| Method                | POST |
| Allowed Scopes        | ManageScopes |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | None |
| Payload               | `ScopeCreateRequestDto` |
| Request Attribute Definition | See below |
| Response Payload      | `ScopeListRepresentation` |
| Response Status       | <ul><li>201 - Created: Scope created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: Scope already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **name**: String (required, unique, validated)
- **description**: String (required, max 255 chars)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| name | Required. Unique scope name. Must match pattern and length. |
| description | Required. Scope description. |

#### Response Payload Attributes
- **scopes**: List<ScopeRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| scopes | List of created scope objects |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Scope Management MS" #LightGreen
participant "ScopeMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/scopes (payload, headers)
api-gateway -> ScopeMgmtMS: Forward request
ScopeMgmtMS -> ScopeMgmtMS: Validate payload fields
ScopeMgmtMS -> PostgresDB: Check for duplicate scope
alt Scope Exists
PostgresDB -> ScopeMgmtMS: Scope found
ScopeMgmtMS -> api-gateway: 409 Conflict
api-gateway -> AdminUser: 409 Conflict
else Scope Not Exists
PostgresDB -> ScopeMgmtMS: No scope found
ScopeMgmtMS -> PostgresDB: Save scope entity
ScopeMgmtMS -> api-gateway: 201 Created (scope details)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
ScopeMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ScopeMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 2. Get Scope By Name

### API Description
Retrieves a scope by its unique name. Validates the name, checks existence, and returns details. Handles exceptions for not found or invalid name.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/scopes/{name}` |
| Method                | GET |
| Allowed Scopes        | ManageScopes |
| Request Headers       | None |
| Request Parameters    | `name` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `name`: Required. Scope name. |
| Response Payload      | `ScopeListRepresentation` |
| Response Status       | <ul><li>200 - OK: Scope found</li><li>404 - Not Found: Scope does not exist</li><li>400 - Bad Request: Invalid scope name</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **scopes**: List<ScopeRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| scopes | List of scope objects matching name |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Scope Management MS" #LightGreen
participant "ScopeMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: GET /v1/scopes/{name}
api-gateway -> ScopeMgmtMS: Forward request
ScopeMgmtMS -> PostgresDB: Find scope by name
alt Scope Found
PostgresDB -> ScopeMgmtMS: Scope entity
ScopeMgmtMS -> api-gateway: 200 OK (scope details)
api-gateway -> AdminUser: 200 OK
else Scope Not Found
PostgresDB -> ScopeMgmtMS: No scope found
ScopeMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid Name
ScopeMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ScopeMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 3. Get All Scopes

### API Description
Retrieves all scopes in the system. Supports pagination and filtering. Handles exceptions for system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/scopes` |
| Method                | GET |
| Allowed Scopes        | ManageScopes, ViewScopes |
| Request Headers       | None |
| Request Parameters    | `page` (query, optional), `pageSize` (query, optional) |
| Payload               | None |
| Request Attribute Definition | None |
| Response Payload      | `ScopeListRepresentation` |
| Response Status       | <ul><li>200 - OK: Scopes found</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **scopes**: List<ScopeRepresentation>
- **totalCount**: Integer
- **page**: Integer
- **size**: Integer

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| scopes | List of scope objects |
| totalCount | Total number of scopes |
| page | Current page number |
| size | Page size |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Scope Management MS" #LightGreen
participant "ScopeMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: GET /v1/scopes (query params)
api-gateway -> ScopeMgmtMS: Forward request
ScopeMgmtMS -> PostgresDB: Query all scopes
PostgresDB -> ScopeMgmtMS: Paginated scope list
ScopeMgmtMS -> api-gateway: 200 OK (scopes, count, page, size)
api-gateway -> AdminUser: 200 OK
else Unexpected Error
ScopeMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 4. Update Scope

### API Description
Updates details of a scope identified by its unique name. Validates input, checks permissions, and updates allowed fields. Handles exceptions for invalid data, not found scopes, and persistence errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/scopes/{name}` |
| Method                | PATCH |
| Allowed Scopes        | ManageScopes |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | `name` (path, required, String) |
| Payload               | `ScopePatch` |
| Request Attribute Definition | See below |
| Response Payload      | `ScopeListRepresentation` |
| Response Status       | <ul><li>200 - OK: Scope updated</li><li>400 - Bad Request: Validation failed</li><li>404 - Not Found: Scope does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **description**: String (optional, max 255 chars)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| description | Optional. Scope description. |

#### Response Payload Attributes
- **scopes**: List<ScopeRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| scopes | List of updated scope objects |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Scope Management MS" #LightGreen
participant "ScopeMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PATCH /v1/scopes/{name} (payload, headers)
api-gateway -> ScopeMgmtMS: Forward request
ScopeMgmtMS -> PostgresDB: Find scope by name
alt Scope Found
PostgresDB -> ScopeMgmtMS: Scope entity
ScopeMgmtMS -> PostgresDB: Save updated scope
ScopeMgmtMS -> api-gateway: 200 OK (updated scope)
api-gateway -> AdminUser: 200 OK
else Scope Not Found
PostgresDB -> ScopeMgmtMS: No scope found
ScopeMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Validation Error
ScopeMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ScopeMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 5. Delete Scope

### API Description
Deletes a scope identified by its unique name. Validates the name, checks permissions, and marks the scope as deleted. Handles exceptions for not found scopes, invalid name, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/scopes/{name}` |
| Method                | DELETE |
| Allowed Scopes        | ManageScopes |
| Request Headers       | `loggedInUserId` (required), `scope` (required) |
| Request Parameters    | `name` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `name`: Required. Scope name. |
| Response Payload      | `ScopeListRepresentation` |
| Response Status       | <ul><li>200 - OK: Scope deleted</li><li>404 - Not Found: Scope does not exist</li><li>400 - Bad Request: Invalid scope name</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **scopes**: List<ScopeRepresentation>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| scopes | List of scopes after deletion |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Scope Management MS" #LightGreen
participant "ScopeMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/scopes/{name} (headers)
api-gateway -> ScopeMgmtMS: Forward request
ScopeMgmtMS -> PostgresDB: Find scope by name
alt Scope Found
PostgresDB -> ScopeMgmtMS: Scope entity
ScopeMgmtMS -> PostgresDB: Mark scope as deleted
ScopeMgmtMS -> api-gateway: 200 OK (scopes)
api-gateway -> AdminUser: 200 OK
else Scope Not Found
PostgresDB -> ScopeMgmtMS: No scope found
ScopeMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid Name
ScopeMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ScopeMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```
