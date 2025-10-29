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
