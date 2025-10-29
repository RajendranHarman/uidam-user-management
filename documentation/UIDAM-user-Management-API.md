# UIDAM User Management API Documentation

## User Management

### 1. User Creation API (v1) - Internal User Creation by Admin

### API Description
This API allows an admin user (with `ManageUsers` scope) to create a new internal user in the UIDAM system. The flow covers:
- **Username and password validation**: Ensures username matches pattern and password meets policy.
- **Permission validation**: Checks if the admin user has permission to create users (unless self-add).
- **Account and roles validation**: Validates that all provided roles exist and maps the user to the default tenant account and roles.
- **Duplicate user check**: Ensures the username does not already exist (throws `RecordAlreadyExistsException` if found).
- **Additional attributes validation**: If enabled, checks for mandatory and valid custom attributes.
- **User status lifecycle**: Sets user status to PENDING if user status lifecycle or email verification is enabled, otherwise ACTIVE.
- **Password hashing and salting**: Hashes the password using tenant-specific encoder and stores salt.
- **User entity creation**: Maps DTO to entity, sets account-role mappings, and saves user in database.
- **Account-role mapping**: Associates user with account and roles, sets userId in mapping.
- **Metrics update**: Increments metrics for user creation.
- **Additional attributes persistence**: Saves custom attributes if present and valid.
- **Password history**: Stores password history for the user.
- **Email verification notification**: Triggers notification for email verification (logs warning if notification fails).
- **Returns created user details**.

All validation and exception flows are handled, including:
- Invalid username/password (400 Bad Request)
- Missing/invalid roles or accounts (400 Bad Request)
- Duplicate user (409 Conflict)
- Invalid or duplicate additional attributes (400 Bad Request)
- Database or system errors (500 Internal Server Error)

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users` (Internal) |
| Method                | POST |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | See below for full attribute list |
| Request Attribute Definition | See below for full attribute definitions |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>201 - Created: User successfully created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: User already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **userName**: String (required, unique, validated by regex and length)
- **password**: String (required, validated by custom password rules)
- **roles**: Set<String> (required, at least one role)
- **status**: Enum UserStatus (optional, default: PENDING, values: ACTIVE, INACTIVE, PENDING, etc.)
- **isExternalUser**: Boolean (optional, default: false)
- **aud**: String (optional, client id/audience)
- **firstName**: String (optional, max 49 chars)
- **lastName**: String (optional, max 49 chars)
- **country**: String (optional, max 50 chars)
- **state**: String (optional, max 50 chars)
- **city**: String (optional, max 50 chars)
- **address1**: String (optional, max 50 chars)
- **address2**: String (optional, max 100 chars)
- **postalCode**: String (optional, max 11 chars)
- **phoneNumber**: String (optional, validated by regex, max 16 chars)
- **email**: String (required, validated by email format, max 128 chars)
- **gender**: Enum Gender (optional, values: MALE, FEMALE, etc.)
- **birthDate**: String (optional, ISO 8601 format yyyy-MM-dd)
- **locale**: String (optional, max 35 chars)
- **notificationConsent**: Boolean (optional)
- **timeZone**: String (optional)
- **additionalAttributes**: Map<String, Object> (optional, for custom attributes)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| userName | Required. Unique username. Must match pattern and length. |
| password | Required. Must meet password policy. |
| roles | Required. At least one role. |
| email | Required. Valid email format. |
| isExternalUser | Optional. Default false. |
| aud | Optional. Audience/client id. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, gender, birthDate, locale, notificationConsent, timeZone, additionalAttributes | Optional. See above for validation and allowed values. |
| status | Optional. Enum: ACTIVE, INACTIVE, PENDING, etc. |
| gender | Optional. Enum: MALE, FEMALE, etc. |
| birthDate | Optional. Format: yyyy-MM-dd |
| notificationConsent | Optional. Boolean |
| additionalAttributes | Optional. Map<String, Object> |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"
participant "Notification Center"

AdminUser -> api-gateway: POST /v1/users (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate username pattern, password policy
UserMgmtMS -> UserMgmtMS: Validate admin permissions (unless self-add)
UserMgmtMS -> UserMgmtMS: validateAccountAndRoles(userDto)
UserMgmtMS -> UserMgmtMS: Check for duplicate user in DB
UserMgmtMS -> PostgresDB: findByUserNameIgnoreCaseAndStatusNot
alt User Exists
PostgresDB -> UserMgmtMS: User found
UserMgmtMS -> api-gateway: 409 Conflict (User exists)
api-gateway -> AdminUser: 409 Conflict
else User Not Exists
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> UserMgmtMS: Validate additional attributes (if enabled)
UserMgmtMS -> UserMgmtMS: getIsUserStatusLifeCycleEnabled()
UserMgmtMS -> UserMgmtMS: Set user status (PENDING/ACTIVE)
UserMgmtMS -> UserMgmtMS: PasswordUtils.getSalt()
UserMgmtMS -> UserMgmtMS: PasswordUtils.getSecurePassword()
UserMgmtMS -> UserMgmtMS: UserMapper.mapToUser(userDto)
UserMgmtMS -> UserMgmtMS: mapToAccountsAndRoles(userDto, loggedInUserId)
UserMgmtMS -> UserMgmtMS: Set user addresses, account-role mapping
UserMgmtMS -> PostgresDB: Save user entity
UserMgmtMS -> PostgresDB: Save account-role mapping
UserMgmtMS -> UserMgmtMS: Set userId in mapping
UserMgmtMS -> UserMgmtMS: uidamMetricsService.incrementCounter
UserMgmtMS -> UserMgmtMS: addRoleNamesAndMapToUserResponse
UserMgmtMS -> UserMgmtMS: persistAdditionalAttributes (if present)
UserMgmtMS -> PostgresDB: Save additional attributes
UserMgmtMS -> PostgresDB: Save password history
UserMgmtMS -> Notification Center: Send email verification
alt Notification Success
Notification Center -> UserMgmtMS: Notification sent
else Notification Failure
Notification Center -> UserMgmtMS: Exception (log warning)
end
UserMgmtMS -> api-gateway: 201 Created (User details)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 2. Get User by ID (v1)

#### API Description
This API retrieves a single user identified by their unique user ID. It validates the user ID, checks if the user exists and is not deleted, and returns the user's details. The flow includes:
- Validation of user ID format and presence
- Tenant resolution and context update
- Database lookup for user by ID (excluding deleted status)
- Returns user details if found
- Handles exceptions for not found or inactive users

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/{id}` |
| Method                | GET |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | `id` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `id`: Required. User unique identifier (BigInteger). |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>200 - OK: User found</li><li>404 - Not Found: User does not exist</li><li>400 - Bad Request: Invalid user ID</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor Actor
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

Actor -> api-gateway: GET /v1/users/{id} (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user ID
UserMgmtMS -> UserMgmtMS: Resolve tenantId
UserMgmtMS -> UserMgmtMS: Update tenantContext
UserMgmtMS -> PostgresDB: Find user by ID (exclude deleted)
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> api-gateway: 200 OK (User details)
api-gateway -> Actor: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> Actor: 404 Not Found
end
else Invalid ID
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> Actor: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> Actor: 500 Internal Server Error
end
@enduml
```

### 3. Get External User by ID (v1)

#### API Description
Retrieves an external user by their unique user ID. Validates the user ID, ensures the user is marked as external, and returns user details if found. Handles exceptions for not found, inactive, or non-external users.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/external/{id}` |
| Method                | GET |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | `id` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `id`: Required. User unique identifier (BigInteger). Must be external user. |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>200 - OK: User found</li><li>404 - Not Found: User does not exist or not external</li><li>400 - Bad Request: Invalid user ID</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor Actor
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

Actor -> api-gateway: GET /v1/users/external/{id} (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user ID
UserMgmtMS -> UserMgmtMS: Resolve tenantId
UserMgmtMS -> UserMgmtMS: Update tenantContext
UserMgmtMS -> PostgresDB: Find user by ID (exclude deleted)
alt User Found & External
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> api-gateway: 200 OK (User details)
api-gateway -> Actor: 200 OK
else User Not Found or Not External
PostgresDB -> UserMgmtMS: No user found or not external
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> Actor: 404 Not Found
end
else Invalid ID
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> Actor: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> Actor: 500 Internal Server Error
end
@enduml
```

### 4. Get User Attributes (v1)

#### API Description
Retrieves all user attribute metadata for the tenant, including definitions, types, validation rules, and mandatory/optional status. Used for dynamic forms and validation in user creation/update flows.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/attributes` |
| Method                | GET |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory) |
| Request Parameters    | None |
| Payload               | None |
| Request Attribute Definition | None |
| Response Payload      | List of attribute metadata objects |
| Response Status       | <ul><li>200 - OK: Metadata found</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **attributeName**: String
- **type**: String (e.g., String, Number, Boolean)
- **mandatory**: Boolean
- **validationPattern**: String (regex, optional)
- **maxLength**: Integer (optional)
- **allowedValues**: List<String> (optional)
- **description**: String (optional)

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| attributeName | Name of the attribute |
| type | Data type |
| mandatory | Whether attribute is required |
| validationPattern | Regex for validation |
| maxLength | Maximum allowed length |
| allowedValues | List of allowed values |
| description | Description of attribute |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor Actor
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

Actor -> api-gateway: GET /v1/users/attributes (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Resolve tenantId
UserMgmtMS -> PostgresDB: Query attribute metadata
PostgresDB -> UserMgmtMS: Attribute metadata list
UserMgmtMS -> api-gateway: 200 OK (metadata)
api-gateway -> Actor: 200 OK
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> Actor: 500 Internal Server Error
end
@enduml
```

### 5. Update User Attributes (v1)

#### API Description
Allows updating custom attributes for a user. Validates attribute names, types, and values against metadata. Handles exceptions for invalid attributes, missing mandatory attributes, and persistence errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/attributes` |
| Method                | PUT |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | Map<String, Object> (attributes to update) |
| Request Attribute Definition | See below for attribute validation |
| Response Payload      | Updated attribute metadata |
| Response Status       | <ul><li>200 - OK: Attributes updated</li><li>400 - Bad Request: Validation failed</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **attributes**: Map<String, Object> (attribute name-value pairs)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| attributes | Map of attribute name to value. Each must match metadata type, pattern, and mandatory status. |

#### Response Payload Attributes
- **updatedAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| updatedAttributes | Map of updated attribute name-value pairs |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PUT /v1/users/attributes (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate attributes against metadata
UserMgmtMS -> UserMgmtMS: Check mandatory attributes
UserMgmtMS -> UserMgmtMS: Validate types and patterns
UserMgmtMS -> PostgresDB: Update attributes
alt Success
PostgresDB -> UserMgmtMS: Attributes updated
UserMgmtMS -> api-gateway: 200 OK (updated attributes)
api-gateway -> AdminUser: 200 OK
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 6. Update User by ID (v1)

#### API Description
Updates user details for a given user ID. Validates input, checks permissions, and updates allowed fields. Handles exceptions for invalid data, not found users, and persistence errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/{id}` |
| Method                | PATCH |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | `id` (path, required, BigInteger) |
| Payload               | Partial user update object |
| Request Attribute Definition | See below for updatable fields |
| Response Payload      | Updated user details |
| Response Status       | <ul><li>200 - OK: User updated</li><li>400 - Bad Request: Validation failed</li><li>404 - Not Found: User does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **firstName**: String (optional, max 49 chars)
- **lastName**: String (optional, max 49 chars)
- **country**: String (optional, max 50 chars)
- **state**: String (optional, max 50 chars)
- **city**: String (optional, max 50 chars)
- **address1**: String (optional, max 50 chars)
- **address2**: String (optional, max 100 chars)
- **postalCode**: String (optional, max 11 chars)
- **phoneNumber**: String (optional, validated by regex, max 16 chars)
- **email**: String (optional, validated by email format, max 128 chars)
- **gender**: Enum Gender (optional)
- **birthDate**: String (optional, ISO 8601 format yyyy-MM-dd)
- **locale**: String (optional, max 35 chars)
- **notificationConsent**: Boolean (optional)
- **timeZone**: String (optional)
- **additionalAttributes**: Map<String, Object> (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| All above | Optional. Must match validation rules. |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PATCH /v1/users/{id} (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user ID
UserMgmtMS -> UserMgmtMS: Validate payload fields
UserMgmtMS -> UserMgmtMS: Check permissions
UserMgmtMS -> PostgresDB: Find user by ID
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> UserMgmtMS: Update allowed fields
UserMgmtMS -> PostgresDB: Save updated user
UserMgmtMS -> api-gateway: 200 OK (updated user)
api-gateway -> AdminUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 7. Delete User by ID (v1)

#### API Description
Deletes a user identified by their unique user ID. Validates the user ID, checks permissions, and marks the user as deleted (soft delete). Handles exceptions for not found users, invalid ID, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/{id}` |
| Method                | DELETE |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | `id` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `id`: Required. User unique identifier (BigInteger). |
| Response Payload      | Success message |
| Response Status       | <ul><li>200 - OK: User deleted</li><li>404 - Not Found: User does not exist</li><li>400 - Bad Request: Invalid user ID</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **message**: String (e.g., "User deleted successfully")

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| message | Confirmation message |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/users/{id} (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user ID
UserMgmtMS -> UserMgmtMS: Check permissions
UserMgmtMS -> PostgresDB: Find user by ID
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> PostgresDB: Mark user as deleted (soft delete)
UserMgmtMS -> api-gateway: 200 OK (message)
api-gateway -> AdminUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid ID
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 8. Delete Users by Filter (v1)

#### API Description
Deletes multiple users matching a filter criteria. Validates filter, checks permissions, and marks all matching users as deleted (soft delete). Handles exceptions for invalid filter, not found users, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users` |
| Method                | DELETE |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | Filter object |
| Request Attribute Definition | See below for filter attributes |
| Response Payload      | Success message, count of deleted users |
| Response Status       | <ul><li>200 - OK: Users deleted</li><li>400 - Bad Request: Invalid filter</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **filter**: Object (criteria for user selection)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| filter | Object containing filter criteria (e.g., status, role, account, etc.) |

#### Response Payload Attributes
- **message**: String (e.g., "Users deleted successfully")
- **deletedCount**: Integer (number of users deleted)

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| message | Confirmation message |
| deletedCount | Number of users deleted |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/users (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate filter
UserMgmtMS -> UserMgmtMS: Check permissions
UserMgmtMS -> PostgresDB: Find users by filter
alt Users Found
PostgresDB -> UserMgmtMS: User entities
UserMgmtMS -> PostgresDB: Mark users as deleted (soft delete)
UserMgmtMS -> api-gateway: 200 OK (message, count)
api-gateway -> AdminUser: 200 OK
else No Users Found
PostgresDB -> UserMgmtMS: No users found
UserMgmtMS -> api-gateway: 200 OK (message, count=0)
api-gateway -> AdminUser: 200 OK
end
else Invalid Filter
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 9. Filter Users (v1)

#### API Description
Filters users based on provided criteria such as status, roles, account, attributes, and pagination. Validates filter object, applies query, and returns paginated user list. Handles exceptions for invalid filter, no results, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/filter` |
| Method                | POST |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | Filter object |
| Request Attribute Definition | See below for filter attributes |
| Response Payload      | Paginated user list |
| Response Status       | <ul><li>200 - OK: Users found</li><li>400 - Bad Request: Invalid filter</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **status**: Enum UserStatus (optional)
- **roles**: Set<String> (optional)
- **accountId**: BigInteger (optional)
- **attributes**: Map<String, Object> (optional)
- **searchText**: String (optional)
- **page**: Integer (optional, default: 0)
- **size**: Integer (optional, default: 20)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| status | Filter by user status |
| roles | Filter by roles |
| accountId | Filter by account |
| attributes | Filter by custom attributes |
| searchText | Free text search |
| page | Page number |
| size | Page size |

#### Response Payload Attributes
- **users**: List<UserResponseV1> (see user response attributes above)
- **totalCount**: Integer
- **page**: Integer
- **size**: Integer

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| users | List of user objects matching filter |
| totalCount | Total number of users matching filter |
| page | Current page number |
| size | Page size |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor Actor
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

Actor -> api-gateway: POST /v1/users/filter (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate filter object
UserMgmtMS -> PostgresDB: Query users by filter
PostgresDB -> UserMgmtMS: Paginated user list
UserMgmtMS -> api-gateway: 200 OK (users, count, page, size)
api-gateway -> Actor: 200 OK
else Invalid Filter
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> Actor: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> Actor: 500 Internal Server Error
end
@enduml
```

### 10. Get Self User (v1)

#### API Description
Retrieves details of the currently authenticated user (self). Validates authentication, resolves user context, and returns user details. Handles exceptions for not found, inactive, or unauthorized users.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/self` |
| Method                | GET |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (mandatory) |
| Request Parameters    | None |
| Payload               | None |
| Request Attribute Definition | None |
| Response Payload      | See user response attributes above |
| Response Status       | <ul><li>200 - OK: User found</li><li>404 - Not Found: User does not exist</li><li>401 - Unauthorized: Not authenticated</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- See user response attributes above

#### Response Attribute Definitions
- See user response attribute definitions above

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor SelfUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

SelfUser -> api-gateway: GET /v1/users/self (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate authentication
UserMgmtMS -> UserMgmtMS: Resolve user context
UserMgmtMS -> PostgresDB: Find user by loggedInUserId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> api-gateway: 200 OK (user details)
api-gateway -> SelfUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> SelfUser: 404 Not Found
end
else Unauthorized
UserMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> SelfUser: 401 Unauthorized
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> SelfUser: 500 Internal Server Error
end
@enduml
```

### 11. Update Self User (v1)

#### API Description
Updates details of the currently authenticated user (self). Validates authentication, checks allowed fields, applies validation, and updates user record. Handles exceptions for invalid data, unauthorized, and persistence errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/self` |
| Method                | PATCH |
| Allowed Scopes        | ManageUsers, ViewUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (mandatory) |
| Request Parameters    | None |
| Payload               | Partial user update object |
| Request Attribute Definition | See below for updatable fields |
| Response Payload      | Updated user details |
| Response Status       | <ul><li>200 - OK: User updated</li><li>400 - Bad Request: Validation failed</li><li>401 - Unauthorized: Not authenticated</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **firstName**: String (optional, max 49 chars)
- **lastName**: String (optional, max 49 chars)
- **country**: String (optional, max 50 chars)
- **state**: String (optional, max 50 chars)
- **city**: String (optional, max 50 chars)
- **address1**: String (optional, max 50 chars)
- **address2**: String (optional, max 100 chars)
- **postalCode**: String (optional, max 11 chars)
- **phoneNumber**: String (optional, validated by regex, max 16 chars)
- **email**: String (optional, validated by email format, max 128 chars)
- **gender**: Enum Gender (optional)
- **birthDate**: String (optional, ISO 8601 format yyyy-MM-dd)
- **locale**: String (optional, max 35 chars)
- **notificationConsent**: Boolean (optional)
- **timeZone**: String (optional)
- **additionalAttributes**: Map<String, Object> (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| All above | Optional. Must match validation rules. |

#### Response Payload Attributes
- See user response attributes above

#### Response Attribute Definitions
- See user response attribute definitions above

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor SelfUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

SelfUser -> api-gateway: PATCH /v1/users/self (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate authentication
UserMgmtMS -> UserMgmtMS: Validate payload fields
UserMgmtMS -> PostgresDB: Find user by loggedInUserId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> UserMgmtMS: Update allowed fields
UserMgmtMS -> PostgresDB: Save updated user
UserMgmtMS -> api-gateway: 200 OK (updated user)
api-gateway -> SelfUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> SelfUser: 404 Not Found
end
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> SelfUser: 400 Bad Request
end
else Unauthorized
UserMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> SelfUser: 401 Unauthorized
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> SelfUser: 500 Internal Server Error
end
@enduml
```

### 12. Delete Self User (v1)

#### API Description
Deletes the currently authenticated user (self). Validates authentication, checks user existence, and marks user as deleted (soft delete). Handles exceptions for unauthorized, not found, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/self` |
| Method                | DELETE |
| Allowed Scopes        | ManageUsers, ViewUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (mandatory) |
| Request Parameters    | None |
| Payload               | None |
| Request Attribute Definition | None |
| Response Payload      | Success message |
| Response Status       | <ul><li>200 - OK: User deleted</li><li>401 - Unauthorized: Not authenticated</li><li>404 - Not Found: User does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **message**: String (e.g., "User deleted successfully")

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| message | Confirmation message |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor SelfUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

SelfUser -> api-gateway: DELETE /v1/users/self (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate authentication
UserMgmtMS -> PostgresDB: Find user by loggedInUserId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> PostgresDB: Mark user as deleted (soft delete)
UserMgmtMS -> api-gateway: 200 OK (message)
api-gateway -> SelfUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> SelfUser: 404 Not Found
end
else Unauthorized
UserMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> SelfUser: 401 Unauthorized
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> SelfUser: 500 Internal Server Error
end
@enduml
```

### 13. Self User Password Recovery (v1)

#### API Description
Initiates password recovery for the currently authenticated user (self) by sending a password reset link or code to the registered email. Validates authentication, checks user existence, and triggers notification. Handles exceptions for unauthorized, not found, and notification errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/self/recovery/forgot-password` |
| Method                | POST |
| Allowed Scopes        | ViewUsers, ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (mandatory) |
| Request Parameters    | None |
| Payload               | None |
| Request Attribute Definition | None |
| Response Payload      | Success message |
| Response Status       | <ul><li>200 - OK: Recovery initiated</li><li>401 - Unauthorized: Not authenticated</li><li>404 - Not Found: User does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **message**: String (e.g., "Password recovery initiated. Check your email.")

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| message | Confirmation message |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor SelfUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"
participant "Notification Center"

SelfUser -> api-gateway: POST /v1/users/self/recovery/forgot-password (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate authentication
UserMgmtMS -> PostgresDB: Find user by loggedInUserId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> Notification Center: Send password recovery email
alt Notification Success
Notification Center -> UserMgmtMS: Notification sent
UserMgmtMS -> api-gateway: 200 OK (message)
api-gateway -> SelfUser: 200 OK
else Notification Failure
Notification Center -> UserMgmtMS: Exception (log warning)
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> SelfUser: 500 Internal Server Error
end
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> SelfUser: 404 Not Found
end
else Unauthorized
UserMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> SelfUser: 401 Unauthorized
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> SelfUser: 500 Internal Server Error
end
@enduml
```

### 14. Change User Status (v1)

#### API Description
Changes the status of a user (e.g., activate, deactivate, suspend) by user ID. Validates permissions, user existence, and allowed status transitions. Handles exceptions for invalid status, not found, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/change-status` |
| Method                | PATCH |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | Status change object |
| Request Attribute Definition | See below for attributes |
| Response Payload      | Updated user details |
| Response Status       | <ul><li>200 - OK: Status changed</li><li>400 - Bad Request: Invalid status or transition</li><li>404 - Not Found: User does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **userId**: BigInteger (required)
- **newStatus**: Enum UserStatus (required, e.g., ACTIVE, INACTIVE, SUSPENDED)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| userId | User unique identifier |
| newStatus | New status to set |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PATCH /v1/users/change-status (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate permissions
UserMgmtMS -> UserMgmtMS: Validate userId and newStatus
UserMgmtMS -> PostgresDB: Find user by userId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> UserMgmtMS: Validate allowed status transition
UserMgmtMS -> PostgresDB: Update user status
UserMgmtMS -> api-gateway: 200 OK (updated user)
api-gateway -> AdminUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid Status
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 15. Add External User (v1)

#### API Description
Creates a new external user in the UIDAM system. The flow covers:
- **Username and password validation**: Ensures username matches pattern and password meets policy.
- **Permission validation**: Checks if the admin user has permission to create users (self-add not allowed).
- **Account and roles validation**: Validates that all provided roles exist.
- **Duplicate user check**: Ensures the username does not already exist (throws `RecordAlreadyExistsException` if found).
- **Additional attributes validation**: If enabled, checks for mandatory and valid custom attributes.
- **User status lifecycle**: Sets user status to PENDING if user status lifecycle or email verification is enabled, otherwise ACTIVE.
- **Password hashing and salting**: Hashes the password using tenant-specific encoder and stores salt.
- **User entity creation**: Maps DTO to entity, sets account-role mappings, and saves user in database.
- **Account-role mapping**: Associates user with roles.
- **Metrics update**: Increments metrics for user creation.
- **Additional attributes persistence**: Saves custom attributes if present and valid.
- **Password history**: Stores password history for the user.
- **Email verification notification**: Triggers notification for email verification (logs warning if notification fails).
- **Returns created user details**.

All validation and exception flows are handled, including:
- Invalid username/password (400 Bad Request)
- Missing/invalid roles (400 Bad Request)
- Duplicate user (409 Conflict)
- Invalid or duplicate additional attributes (400 Bad Request)
- Database or system errors (500 Internal Server Error)

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/external` |
| Method                | POST |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | See below for full attribute list |
| Request Attribute Definition | See below for full attribute definitions |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>201 - Created: User successfully created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: User already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **userName**: String (required, unique, validated by regex and length)
- **password**: String (required, validated by custom password rules)
- **roles**: Set<String> (required, at least one role)
- **status**: Enum UserStatus (optional, default: PENDING, values: ACTIVE, INACTIVE, PENDING, etc.)
- **isExternalUser**: Boolean (required, true)
- **aud**: String (optional, client id/audience)
- **firstName**: String (optional, max 49 chars)
- **lastName**: String (optional, max 49 chars)
- **country**: String (optional, max 50 chars)
- **state**: String (optional, max 50 chars)
- **city**: String (optional, max 50 chars)
- **address1**: String (optional, max 50 chars)
- **address2**: String (optional, max 100 chars)
- **postalCode**: String (optional, max 11 chars)
- **phoneNumber**: String (optional, validated by regex, max 16 chars)
- **email**: String (required, validated by email format, max 128 chars)
- **gender**: Enum Gender (optional, values: MALE, FEMALE, etc.)
- **birthDate**: String (optional, ISO 8601 format yyyy-MM-dd)
- **locale**: String (optional, max 35 chars)
- **notificationConsent**: Boolean (optional)
- **timeZone**: String (optional)
- **additionalAttributes**: Map<String, Object> (optional, for custom attributes)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| userName | Required. Unique username. Must match pattern and length. |
| password | Required. Must meet password policy. |
| roles | Required. At least one role. |
| email | Required. Valid email format. |
| isExternalUser | Required. Must be true. |
| aud | Optional. Audience/client id. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, gender, birthDate, locale, notificationConsent, timeZone, additionalAttributes | Optional. See above for validation and allowed values. |
| status | Optional. Enum: ACTIVE, INACTIVE, PENDING, etc. |
| gender | Optional. Enum: MALE, FEMALE, etc. |
| birthDate | Optional. Format: yyyy-MM-dd |
| notificationConsent | Optional. Boolean |
| additionalAttributes | Optional. Map<String, Object> |

#### Response Payload Attributes
- **id**: BigInteger (user id)
- **userName**: String
- **status**: Enum UserStatus
- **firstName**: String
- **lastName**: String
- **country**: String
- **state**: String
- **city**: String
- **address1**: String
- **address2**: String
- **postalCode**: String
- **phoneNumber**: String
- **email**: String
- **gender**: Enum Gender
- **birthDate**: String (ISO 8601)
- **locale**: String
- **notificationConsent**: Boolean
- **timeZone**: String
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"
participant "Notification Center"

AdminUser -> api-gateway: POST /v1/users/external (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate payload fields
UserMgmtMS -> UserMgmtMS: Validate roles and attributes
UserMgmtMS -> PostgresDB: Check for duplicate user
alt User Exists
PostgresDB -> UserMgmtMS: User found
UserMgmtMS -> api-gateway: 409 Conflict (User exists)
api-gateway -> AdminUser: 409 Conflict
else User Not Exists
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> UserMgmtMS: Hash password, map DTO to entity
UserMgmtMS -> UserMgmtMS: Set user status to PENDING
UserMgmtMS -> UserMgmtMS: mapToAccountsAndRoles(userDto, loggedInUserId)
UserMgmtMS -> PostgresDB: Save user entity
UserMgmtMS -> PostgresDB: Save account-role mapping
UserMgmtMS -> UserMgmtMS: persistAdditionalAttributes (if present)
UserMgmtMS -> PostgresDB: Save additional attributes
UserMgmtMS -> Notification Center: Send email verification
alt Notification Success
Notification Center -> UserMgmtMS: Notification sent
else Notification Failure
Notification Center -> UserMgmtMS: Exception (log warning)
end
UserMgmtMS -> api-gateway: 201 Created (user details)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

### 16. Delete External User by ID (v1)

#### API Description
Deletes an external user identified by their unique user ID. Validates user ID, checks user type, and marks user as deleted (soft delete). Handles exceptions for not found, not external, and system errors.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/external/{id}` |
| Method                | DELETE |
| Allowed Scopes        | ManageUsers |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | `id` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `id`: Required. User unique identifier. Must be external user. |
| Response Payload      | Success message |
| Response Status       | <ul><li>200 - OK: User deleted</li><li>404 - Not Found: User does not exist or not external</li><li>400 - Bad Request: Invalid user ID</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **message**: String (e.g., "External user deleted successfully")

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| message | Confirmation message |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/users/external/{id} (headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user ID
UserMgmtMS -> PostgresDB: Find user by ID
alt User Found & External
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> PostgresDB: Mark user as deleted (soft delete)
UserMgmtMS -> api-gateway: 200 OK (message)
api-gateway -> AdminUser: 200 OK
else User Not Found or Not External
PostgresDB -> UserMgmtMS: No user found or not external
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid ID
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

---

# Account Management API Documentation

## 1. Associate/Dissociate Accounts to User

**Description:**
Associates or dissociates accounts to a specified user. Requires `ManageUsers` or `ManageUserRolesAndPermissions` scope. Validates user existence, payload structure, and operation type. Throws errors for invalid user, payload, or association failures.

**API Details:**
| Field         | Value                                                      |
|---------------|------------------------------------------------------------|
| Endpoint      | `/v1/users/{userId}/associate-accounts`                   |
| Method        | PATCH                                                     |
| Required Scope| `ManageUsers`, `ManageUserRolesAndPermissions`            |
| Headers       | `loggedInUserId`                                          |
| Path Param    | `userId` (BigInteger, required)                           |
| Payload       | List of `AssociateAccountDto`                             |
| Attributes    | `op` (ADD/REMOVE), `value` (account ID)                   |
| Response      | `AssociateAccountResponse`                                |
| Status Codes  | 200 (OK), 400 (Validation Error), 404 (User Not Found), 500 (Association Error) |

#### Payload Attribute Definitions
| Attribute | Type | Required | Description |
|-----------|------|----------|-------------|
| op        | String | Yes | Operation type: "ADD" or "REMOVE". Must match allowed pattern. |
| value     | String | Yes | Account ID to associate/dissociate. |

#### Response Attribute Definitions
| Attribute | Type | Description |
|-----------|------|-------------|
| accounts  | Set<UserAccounts> | Set of account objects |
| account   | String | Account name |

#### Validation Logic
- `op` must be "ADD" or "REMOVE"
- `value` must be present
- User must exist

#### Exception Handling
- 400: Validation errors
- 404: User not found
- 500: Association failure (`UserAccountMappingException`)

#### Sequence Diagram
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "User Management MS" #LightGreen
participant "UserMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PATCH /v1/users/{userId}/associate-accounts (payload, headers)
api-gateway -> UserMgmtMS: Forward request
UserMgmtMS -> UserMgmtMS: Validate user existence
UserMgmtMS -> UserMgmtMS: Validate payload structure
UserMgmtMS -> UserMgmtMS: Check operation type
UserMgmtMS -> PostgresDB: Find user by userId
alt User Found
PostgresDB -> UserMgmtMS: User entity
UserMgmtMS -> UserMgmtMS: Perform ADD or REMOVE operation
UserMgmtMS -> PostgresDB: Update user-account mapping
UserMgmtMS -> api-gateway: 200 OK (updated accounts)
api-gateway -> AdminUser: 200 OK
else User Not Found
PostgresDB -> UserMgmtMS: No user found
UserMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Validation Error
UserMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
UserMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml

---

# Role Management API Documentation

## 1. Create Role

### API Description
This API allows an admin user to create a new role in the UIDAM system. The flow covers:
- **Role name and description validation**: Ensures role name is unique and description is provided.
- **Permission validation**: Checks if the admin user has permission to create roles.
- **Default permissions assignment**: Assigns default permissions to the new role.
- **Role entity creation**: Maps DTO to entity and saves role in database.
- **Metrics update**: Increments metrics for role creation.
- **Returns created role details**.

All validation and exception flows are handled, including:
- Invalid role name or description (400 Bad Request)
- Duplicate role name (409 Conflict)
- Database or system errors (500 Internal Server Error)

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles` |
| Method                | POST |
| Allowed Scopes        | ManageRoles |
| Request Headers       | `tenantId` (optional), `correlationId` (optional), `scopes` (mandatory), `loggedInUserId` (optional) |
| Request Parameters    | None |
| Payload               | See below for full attribute list |
| Request Attribute Definition | See below for full attribute definitions |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>201 - Created: Role successfully created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: Role already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **roleName**: String (required, unique, validated by regex and length)
- **description**: String (required, max 255 chars)
- **permissions**: Set<String> (optional, default: empty set)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roleName | Required. Unique role name. Must match pattern and length. |
| description | Required. Role description. |
| permissions | Optional. Set of permissions. |

#### Response Payload Attributes
- **id**: BigInteger (role id)
- **roleName**: String
- **description**: String
- **permissions**: Set<String>

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | Role unique identifier (BigInteger) |
| roleName | Role name |
| description | Role description |
| permissions | Set of permissions |

### Flow Diagram (PlantUML)
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
RoleMgmtMS -> RoleMgmtMS: Validate role name and description
RoleMgmtMS -> RoleMgmtMS: Check admin permissions
RoleMgmtMS -> PostgresDB: Check for duplicate role
alt Role Exists
PostgresDB -> RoleMgmtMS: Role found
RoleMgmtMS -> api-gateway: 409 Conflict (Role exists)
api-gateway -> AdminUser: 409 Conflict
else Role Not Exists
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> RoleMgmtMS: Map DTO to entity
RoleMgmtMS -> PostgresDB: Save role entity
RoleMgmtMS -> RoleMgmtMS: uidamMetricsService.incrementCounter
RoleMgmtMS -> api-gateway: 201 Created (Role details)
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
This API retrieves a role identified by its unique role name. It validates the role name, checks if the role exists, and returns the role's details. The flow includes:
- Validation of role name presence
- Database lookup for role by name
- Returns role details if found
- Handles exceptions for not found roles

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/{name}` |
| Method                | GET |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | None |
| Request Parameters    | `name` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `name`: Required. Role unique identifier (String). |
| Response Payload      | `BaseResponse` |
| Response Status       | <ul><li>200 - OK: Role found</li><li>404 - Not Found: Role does not exist</li><li>400 - Bad Request: Invalid role name</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **code**: String
- **message**: String
- **data**: RegisteredClientDetails
- **httpStatus**: HTTP status code

#### Sequence Diagram
```
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Role Management MS" #LightGreen
participant "RoleMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: GET /v1/roles/{name} (headers)
api-gateway -> RoleMgmtMS: Forward request
RoleMgmtMS -> RoleMgmtMS: Validate roleName
RoleMgmtMS -> PostgresDB: Find role by roleName
alt Role Found
PostgresDB -> RoleMgmtMS: Role entity
RoleMgmtMS -> api-gateway: 200 OK (Role details)
api-gateway -> AdminUser: 200 OK
else Role Not Found
PostgresDB -> RoleMgmtMS: No role found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid RoleName
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
This API retrieves one or more roles identified by their unique role IDs. It validates the role IDs, checks if the roles exist, and returns their details. The flow includes:
- Validation of role ID(s) presence and format
- Database lookup for roles by ID
- Returns role details if found
- Handles exceptions for not found roles or invalid IDs

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/roles/by-id` |
| Method                | POST |
| Allowed Scopes        | ManageUserRolesAndPermissions |
| Request Headers       | None |
| Request Parameters    | None |
| Payload               | `RoleIdRequest` (contains list of role IDs) |
| Request Attribute Definition | `roleId`: Required. List of role unique identifiers (BigInteger). |
| Response Payload      | `RoleListRepresentation` |
| Response Status       | <ul><li>200 - OK: Roles found</li><li>404 - Not Found: Role(s) do not exist</li><li>400 - Bad Request: Invalid role ID(s)</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **roleId**: List<BigInteger> (required)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roleId | Required. List of role unique identifiers. |

#### Response Payload Attributes
- **roles**: List<RoleRepresentation> (see role response attributes above)

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| roles | List of role objects matching IDs |

#### Sequence Diagram
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
RoleMgmtMS -> RoleMgmtMS: Validate roleId(s)
RoleMgmtMS -> PostgresDB: Find roles by roleId(s)
alt Roles Found
PostgresDB -> RoleMgmtMS: Role entities
RoleMgmtMS -> api-gateway: 200 OK (Role details)
api-gateway -> AdminUser: 200 OK
else Role(s) Not Found
PostgresDB -> RoleMgmtMS: No roles found
RoleMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid roleId(s)
RoleMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
RoleMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

---
