# Account Management API Documentation

## 1. Create Account

### API Description
Creates a new account in the UIDAM system. Validates account details (name, parentId, roles, status), checks for uniqueness, and required fields. Handles exceptions for invalid data, duplicate account, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/accounts` |
| Method                | POST |
| Allowed Scopes        | ManageAccounts |
| Request Headers       | `loggedInUserId` (required), `correlationId` (optional) |
| Request Parameters    | None |
| Payload               | `CreateAccountDto` |
| Request Attribute Definition | See below |
| Response Payload      | `CreateAccountResponse` |
| Response Status       | <ul><li>201 - Created: Account created</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: Account already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
| Attribute    | Type        | Required | Description |
|--------------|------------|----------|-------------|
| accountName  | String     | Yes      | Unique account name. Must not be blank. |
| parentId     | BigInteger | No       | Parent account ID. Optional. |
| roles        | List<String> | No     | List of role names to assign. Optional. |
| status       | String     | No       | Account status. Allowed: active, disabled. Optional. |

#### Response Payload Attributes
| Attribute    | Type        | Description |
|--------------|------------|-------------|
| accountId    | BigInteger | Unique identifier for created account |
| message      | String     | Status message |

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| accountId | Unique identifier for created account |
| message | Status message |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Account Management MS" #LightGreen
participant "AccountMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/accounts (payload, headers)
api-gateway -> AccountMgmtMS: Forward request
AccountMgmtMS -> AccountMgmtMS: Validate payload fields
AccountMgmtMS -> PostgresDB: Check for duplicate account
alt Account Exists
PostgresDB -> AccountMgmtMS: Account found
AccountMgmtMS -> api-gateway: 409 Conflict
api-gateway -> AdminUser: 409 Conflict
else Account Not Exists
PostgresDB -> AccountMgmtMS: No account found
AccountMgmtMS -> PostgresDB: Save account entity
AccountMgmtMS -> api-gateway: 201 Created (accountId, message)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
AccountMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
AccountMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 2. Delete Account

### API Description
Deletes an account by its unique ID. Validates user identity and permissions. Handles exceptions for not found, unauthorized access, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/accounts/{accountId}` |
| Method                | DELETE |
| Allowed Scopes        | ManageAccounts |
| Request Headers       | `loggedInUserId` (required), `correlationId` (optional) |
| Request Parameters    | `accountId` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `accountId`: Required. Account identifier. |
| Response Payload      | None |
| Response Status       | <ul><li>200 - OK: Account deleted</li><li>404 - Not Found: Account does not exist</li><li>401 - Unauthorized: Access denied</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Attribute Definitions
| Attribute    | Type        | Required | Description |
|--------------|------------|----------|-------------|
| accountId    | BigInteger | Yes      | Unique account identifier |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Account Management MS" #LightGreen
participant "AccountMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/accounts/{accountId} (headers)
api-gateway -> AccountMgmtMS: Forward request
AccountMgmtMS -> PostgresDB: Find account by accountId
alt Account Found
PostgresDB -> AccountMgmtMS: Account entity
AccountMgmtMS -> PostgresDB: Delete account
AccountMgmtMS -> api-gateway: 200 OK
api-gateway -> AdminUser: 200 OK
else Account Not Found
PostgresDB -> AccountMgmtMS: No account found
AccountMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Unauthorized
AccountMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> AdminUser: 401 Unauthorized
end
else Unexpected Error
AccountMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 3. Get Account

### API Description
Retrieves account information by its unique ID. Validates permissions. Handles exceptions for not found, unauthorized access, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/accounts/{accountId}` |
| Method                | GET |
| Allowed Scopes        | ViewAccounts, ManageAccounts |
| Request Headers       | `loggedInUserId` (optional), `correlationId` (optional) |
| Request Parameters    | `accountId` (path, required, BigInteger) |
| Payload               | None |
| Request Attribute Definition | `accountId`: Required. Account identifier. |
| Response Payload      | `GetAccountApiResponse` |
| Response Status       | <ul><li>200 - OK: Account found</li><li>404 - Not Found: Account does not exist</li><li>401 - Unauthorized: Access denied</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
| Attribute    | Type        | Description |
|--------------|------------|-------------|
| accountId    | BigInteger | Unique account identifier |
| accountName  | String     | Account name |
| parentId     | BigInteger | Parent account ID |
| roles        | List<String> | List of assigned roles |
| status       | String     | Account status |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor User
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Account Management MS" #LightGreen
participant "AccountMgmtMS"
end box
participant "PostgresDB"

User -> api-gateway: GET /v1/accounts/{accountId} (headers)
api-gateway -> AccountMgmtMS: Forward request
AccountMgmtMS -> PostgresDB: Find account by accountId
alt Account Found
PostgresDB -> AccountMgmtMS: Account entity
AccountMgmtMS -> api-gateway: 200 OK (account details)
api-gateway -> User: 200 OK
else Account Not Found
PostgresDB -> AccountMgmtMS: No account found
AccountMgmtMS -> api-gateway: 404 Not Found
api-gateway -> User: 404 Not Found
end
else Unauthorized
AccountMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> User: 401 Unauthorized
end
else Unexpected Error
AccountMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> User: 500 Internal Server Error
end
@enduml
```

## 4. Filter Accounts

### API Description
Filters accounts based on criteria such as accountId, parentId, roles, status, with support for sorting and search modes. Validates filter object, applies query, and returns paginated list. Handles exceptions for invalid filter, no results, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/accounts/filter` |
| Method                | POST |
| Allowed Scopes        | ViewAccounts, ManageAccounts |
| Request Headers       | `loggedInUserId` (required), `correlationId` (optional) |
| Request Parameters    | `sortBy` (query, optional), `sortOrder` (query, optional), `ignoreCase` (query, optional), `searchMode` (query, optional) |
| Payload               | `AccountFilterDto` |
| Request Attribute Definition | See below |
| Response Payload      | `FilterAccountsApiResponse` |
| Response Status       | <ul><li>200 - OK: Accounts found</li><li>400 - Bad Request: Invalid filter</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
| Attribute    | Type        | Required | Description |
|--------------|------------|----------|-------------|
| accountId    | BigInteger | No       | Filter by account ID |
| parentId     | BigInteger | No       | Filter by parent account ID |
| roles        | List<String> | No     | Filter by roles |
| status       | String     | No       | Filter by account status |

#### Response Payload Attributes
| Attribute    | Type        | Description |
|--------------|------------|-------------|
| accounts     | List<GetAccountApiResponse> | List of account objects matching filter |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor User
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Account Management MS" #LightGreen
participant "AccountMgmtMS"
end box
participant "PostgresDB"

User -> api-gateway: POST /v1/accounts/filter (payload, query params, headers)
api-gateway -> AccountMgmtMS: Forward request
AccountMgmtMS -> PostgresDB: Query accounts by filter
PostgresDB -> AccountMgmtMS: Filtered account list
AccountMgmtMS -> api-gateway: 200 OK (accounts)
api-gateway -> User: 200 OK
else Invalid Filter
AccountMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> User: 400 Bad Request
end
else Unexpected Error
AccountMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> User: 500 Internal Server Error
end
@enduml
```

## 5. Update Account

### API Description
Updates account details (roles, parentId, status) for a given accountId. Validates input, checks permissions, and updates allowed fields. Handles exceptions for invalid data, not found accounts, and persistence errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/accounts/{accountId}` |
| Method                | POST |
| Allowed Scopes        | ManageAccounts |
| Request Headers       | `loggedInUserId` (required), `correlationId` (optional) |
| Request Parameters    | `accountId` (path, required, BigInteger) |
| Payload               | `UpdateAccountDto` |
| Request Attribute Definition | See below |
| Response Payload      | None |
| Response Status       | <ul><li>200 - OK: Account updated</li><li>400 - Bad Request: Validation failed</li><li>404 - Not Found: Account does not exist</li><li>401 - Unauthorized: Access denied</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
| Attribute    | Type        | Required | Description |
|--------------|------------|----------|-------------|
| roles        | List<String> | No     | List of role names to assign. Optional. |
| parentId     | BigInteger | No       | Parent account ID. Optional. |
| status       | String     | No       | Account status. Allowed: active, disabled. Optional. |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Account Management MS" #LightGreen
participant "AccountMgmtMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/accounts/{accountId} (payload, headers)
api-gateway -> AccountMgmtMS: Forward request
AccountMgmtMS -> PostgresDB: Find account by accountId
alt Account Found
PostgresDB -> AccountMgmtMS: Account entity
AccountMgmtMS -> AccountMgmtMS: Validate payload fields
alt Validation Success
AccountMgmtMS -> PostgresDB: Save updated account
AccountMgmtMS -> api-gateway: 200 OK
api-gateway -> AdminUser: 200 OK
else Validation Error
AccountMgmtMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Account Not Found
PostgresDB -> AccountMgmtMS: No account found
AccountMgmtMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Unauthorized
AccountMgmtMS -> api-gateway: 401 Unauthorized
api-gateway -> AdminUser: 401 Unauthorized
end
else Unexpected Error
AccountMgmtMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```
