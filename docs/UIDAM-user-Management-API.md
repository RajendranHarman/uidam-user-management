# UIDAM User Management API Documentation

## User Management

### 1. User Creation API (v1) - Internal User Creation by Admin

#### API Description
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

#### API Details
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

### 2. Self-Signup (v1)

#### API Description
Allows a user to self-signup and create their own account in the UIDAM system. Validates username and password, checks for duplicate users, applies password policy, and sends verification email if enabled. Handles all validation and exception flows.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/self` |
| Method                | POST |
| Allowed Scopes        | Internal (no scopes required) |
| Request Headers       | None |
| Request Parameters    | None |
| Payload               | See below for full attribute list |
| Request Attribute Definition | See below for full attribute definitions |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>201 - Created: User successfully created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: User already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **userName**: String (required, unique, validated by regex and length)
- **password**: String (required, validated by custom password rules)
- **roles**: Set<String> (required, at least one role)
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
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, gender, birthDate, locale, notificationConsent, timeZone, additionalAttributes | Optional. See above for validation and allowed values. |

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
- **verificationEmailSent**: Boolean

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| id | User unique identifier (BigInteger) |
| userName | Username |
| status | Enum: ACTIVE, INACTIVE, PENDING, etc. |
| verificationEmailSent | Whether verification email was sent |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, email, gender, birthDate, locale, notificationConsent, timeZone, devIds, roles, additionalAttributes | See above for type and meaning. |

---

### 3. Federated User Creation (v1)

#### API Description
Creates a new federated user in the UIDAM system. Accepts federated identity details, validates input, checks for duplicates, and persists the user. Handles all validation and exception flows.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/federated` |
| Method                | POST |
| Allowed Scopes        | Internal (no scopes required) |
| Request Headers       | None |
| Request Parameters    | None |
| Payload               | See below for full attribute list |
| Request Attribute Definition | See below for full attribute definitions |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>201 - Created: User successfully created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: User already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **userName**: String (required, unique, validated by regex and length)
- **federatedId**: String (required, federated identity provider ID)
- **roles**: Set<String> (required, at least one role)
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
| federatedId | Required. Federated identity provider ID. |
| roles | Required. At least one role. |
| email | Required. Valid email format. |
| firstName, lastName, country, state, city, address1, address2, postalCode, phoneNumber, gender, birthDate, locale, notificationConsent, timeZone, additionalAttributes | Optional. See above for validation and allowed values. |

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

---

### 4. Get User by Username (v1)

#### API Description
Retrieves user details by username for login or lookup purposes. Validates username, checks if user exists and is active, and returns user details. Handles exceptions for not found or inactive users.

#### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/users/{username}/by-username` |
| Method                | GET |
| Allowed Scopes        | Internal (no scopes required) |
| Request Headers       | `accountName` (optional) |
| Request Parameters    | `username` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `username`: Required. User's username. |
| Response Payload      | See below for full attribute list |
| Response Status       | <ul><li>200 - OK: User found</li><li>404 - Not Found: User does not exist</li><li>400 - Bad Request: Invalid username</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

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


### 5. Add External User (v1)

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

### 6. Get User by ID (v1)

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

### 7. Filter Users (v1)

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

### 8. Update User by ID (v1)

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
- **devIds**: Set<String> (device IDs)
- **roles**: Set<String>
- **additionalAttributes**: Map<String, Object>

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

### 9. Delete User by ID (v1)

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

### 10. Delete Users by Filter (v1)

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

### 11. Get Self User (v1)

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

### 12. Update Self User (v1)

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
- **additionalAttributes**: Map<String, Object>

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| All above | Optional. Must match validation rules. |

#### Response Payload Attributes
- See user response attributes above

#### Response Attribute Definitions
- See user response attribute definitions above

### 13. Delete Self User (v1)

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

### 14. Self User Password Recovery (v1)

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

### 15. Change User Status (v1)

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

### 15. Delete External User by ID (v1)

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

### 16. Get External User by ID (v1)

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

### 17. Get User Attributes (v1)

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

### 18. Update User Attributes (v1)

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

