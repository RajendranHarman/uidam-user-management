# OAuth2 Client Registration API Documentation

## 1. Register OAuth2 Client

### API Description
Registers a new OAuth2 client in the UIDAM system. Validates client details, allowed grant types, and uniqueness. Handles exceptions for invalid data, duplicate client, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/client-registration` |
| Method                | POST |
| Allowed Scopes        | OAuth2ClientMgmt |
| Request Headers       | None |
| Request Parameters    | None |
| Payload               | `RegisteredClientDetails` |
| Request Attribute Definition | See below |
| Response Payload      | `BaseResponse` |
| Response Status       | <ul><li>201 - Created: Client registered</li><li>400 - Bad Request: Validation failed</li><li>409 - Conflict: Client already exists</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **clientId**: String (required, unique, validated)
- **clientSecret**: String (required, validated)
- **grantTypes**: Set<String> (required, allowed values)
- **redirectUris**: Set<String> (optional)
- **scopes**: Set<String> (optional)
- **clientName**: String (optional)
- **clientDescription**: String (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| clientId | Required. Unique client identifier. |
| clientSecret | Required. Client secret. Must meet security policy. |
| grantTypes | Required. Allowed OAuth2 grant types. |
| redirectUris | Optional. Allowed redirect URIs. |
| scopes | Optional. Allowed scopes. |
| clientName | Optional. Client name. |
| clientDescription | Optional. Client description. |

#### Response Payload Attributes
- **code**: String
- **message**: String
- **data**: RegisteredClientDetails
- **httpStatus**: HTTP status code

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| code | Response code |
| message | Response message |
| data | Registered client details |
| httpStatus | HTTP status code |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Client Registration MS" #LightGreen
participant "ClientRegMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: POST /v1/client-registration (payload)
api-gateway -> ClientRegMS: Forward request
ClientRegMS -> ClientRegMS: Validate payload fields
ClientRegMS -> PostgresDB: Check for duplicate clientId
alt Client Exists
PostgresDB -> ClientRegMS: Client found
ClientRegMS -> api-gateway: 409 Conflict
api-gateway -> AdminUser: 409 Conflict
else Client Not Exists
PostgresDB -> ClientRegMS: No client found
ClientRegMS -> PostgresDB: Save client entity
ClientRegMS -> api-gateway: 201 Created (client details)
api-gateway -> AdminUser: 201 Created
end
else Validation Error
ClientRegMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ClientRegMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 2. Get Registered Client By Id

### API Description
Retrieves details of a registered OAuth2 client by its unique clientId. Validates clientId, checks existence, and returns details. Handles exceptions for not found or invalid clientId.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/client-registration/{clientId}` |
| Method                | GET |
| Allowed Scopes        | OAuth2ClientMgmt |
| Request Headers       | None |
| Request Parameters    | `clientId` (path, required, String), `status` (query, optional, String) |
| Payload               | None |
| Request Attribute Definition | `clientId`: Required. Client identifier. |
| Response Payload      | `BaseResponse` |
| Response Status       | <ul><li>200 - OK: Client found</li><li>404 - Not Found: Client does not exist</li><li>400 - Bad Request: Invalid clientId</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **code**: String
- **message**: String
- **data**: RegisteredClientDetails
- **httpStatus**: HTTP status code

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| code | Response code |
| message | Response message |
| data | Registered client details |
| httpStatus | HTTP status code |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Client Registration MS" #LightGreen
participant "ClientRegMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: GET /v1/client-registration/{clientId} (query params)
api-gateway -> ClientRegMS: Forward request
ClientRegMS -> PostgresDB: Find client by clientId
alt Client Found
PostgresDB -> ClientRegMS: Client entity
ClientRegMS -> api-gateway: 200 OK (client details)
api-gateway -> AdminUser: 200 OK
else Client Not Found
PostgresDB -> ClientRegMS: No client found
ClientRegMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid clientId
ClientRegMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ClientRegMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 3. Update Registered Client

### API Description
Updates details of a registered OAuth2 client by its unique clientId. Validates input, checks permissions, and updates allowed fields. Handles exceptions for invalid data, not found clients, and persistence errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/client-registration/{clientId}` |
| Method                | PUT |
| Allowed Scopes        | OAuth2ClientMgmt |
| Request Headers       | None |
| Request Parameters    | `clientId` (path, required, String) |
| Payload               | `RegisteredClientDetails` |
| Request Attribute Definition | See below |
| Response Payload      | `BaseResponse` |
| Response Status       | <ul><li>200 - OK: Client updated</li><li>400 - Bad Request: Validation failed</li><li>404 - Not Found: Client does not exist</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Request Payload Attributes
- **clientId**: String (required)
- **clientSecret**: String (optional)
- **grantTypes**: Set<String> (optional)
- **redirectUris**: Set<String> (optional)
- **scopes**: Set<String> (optional)
- **clientName**: String (optional)
- **clientDescription**: String (optional)

#### Request Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| clientId | Required. Client identifier. |
| clientSecret | Optional. Client secret. |
| grantTypes | Optional. Allowed OAuth2 grant types. |
| redirectUris | Optional. Allowed redirect URIs. |
| scopes | Optional. Allowed scopes. |
| clientName | Optional. Client name. |
| clientDescription | Optional. Client description. |

#### Response Payload Attributes
- **code**: String
- **message**: String
- **data**: RegisteredClientDetails
- **httpStatus**: HTTP status code

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| code | Response code |
| message | Response message |
| data | Registered client details |
| httpStatus | HTTP status code |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Client Registration MS" #LightGreen
participant "ClientRegMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: PUT /v1/client-registration/{clientId} (payload)
api-gateway -> ClientRegMS: Forward request
ClientRegMS -> PostgresDB: Find client by clientId
alt Client Found
PostgresDB -> ClientRegMS: Client entity
ClientRegMS -> PostgresDB: Save updated client
ClientRegMS -> api-gateway: 200 OK (updated client)
api-gateway -> AdminUser: 200 OK
else Client Not Found
PostgresDB -> ClientRegMS: No client found
ClientRegMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Validation Error
ClientRegMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ClientRegMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

## 4. Delete Registered Client

### API Description
Deletes a registered OAuth2 client by its unique clientId. Validates the clientId, checks permissions, and marks the client as deleted. Handles exceptions for not found clients, invalid clientId, and system errors.

### API Details
| Key                   | Value |
|-----------------------|-------|
| End-point             | `/v1/client-registration/{clientId}` |
| Method                | DELETE |
| Allowed Scopes        | OAuth2ClientMgmt |
| Request Headers       | None |
| Request Parameters    | `clientId` (path, required, String) |
| Payload               | None |
| Request Attribute Definition | `clientId`: Required. Client identifier. |
| Response Payload      | Success message |
| Response Status       | <ul><li>200 - OK: Client deleted</li><li>404 - Not Found: Client does not exist</li><li>400 - Bad Request: Invalid clientId</li><li>500 - Internal Server Error: Unexpected error</li></ul> |

#### Response Payload Attributes
- **code**: String
- **message**: String
- **data**: null
- **httpStatus**: HTTP status code

#### Response Attribute Definitions
| Attribute | Definition |
|-----------|------------|
| code | Response code |
| message | Response message |
| data | null |
| httpStatus | HTTP status code |

#### Flow Diagram (PlantUML)
```plantuml
@startuml
actor AdminUser
box "API Gateway" #LightBlue
participant "api-gateway"
end box
box "Client Registration MS" #LightGreen
participant "ClientRegMS"
end box
participant "PostgresDB"

AdminUser -> api-gateway: DELETE /v1/client-registration/{clientId}
api-gateway -> ClientRegMS: Forward request
ClientRegMS -> PostgresDB: Find client by clientId
alt Client Found
PostgresDB -> ClientRegMS: Client entity
ClientRegMS -> PostgresDB: Mark client as deleted
ClientRegMS -> api-gateway: 200 OK (message)
api-gateway -> AdminUser: 200 OK
else Client Not Found
PostgresDB -> ClientRegMS: No client found
ClientRegMS -> api-gateway: 404 Not Found
api-gateway -> AdminUser: 404 Not Found
end
else Invalid clientId
ClientRegMS -> api-gateway: 400 Bad Request
api-gateway -> AdminUser: 400 Bad Request
end
else Unexpected Error
ClientRegMS -> api-gateway: 500 Internal Server Error
api-gateway -> AdminUser: 500 Internal Server Error
end
@enduml
```

---
