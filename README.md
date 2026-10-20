# Project #04 — JWT & Refresh Token Security Lab

A Spring Boot REST API demonstrating stateless authentication with JSON Web Tokens (JWTs), BCrypt password hashing, and persistent refresh-token management using MySQL.

> **Project status:** Implementation and testing are in progress. Review the limitations section before treating this as production-ready.

## Overview

This project explores how an authentication service can issue short-lived access tokens and longer-lived refresh tokens, while storing only refresh-token hashes in the database.

### Key features

- User registration with BCrypt password hashing
- Username/password authentication through Spring Security
- Signed JWT access tokens
- Stateless request authentication through a custom JWT filter
- Secure random refresh tokens; only their SHA-256 hashes are persisted
- Refresh-token rotation and device metadata
- Logout for a supplied refresh token
- Logout from all devices by revoking the user's active refresh tokens
- Request DTO validation and centralized exception handling

## Technology stack

- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- JSON Web Tokens (JJWT)
- Jakarta Bean Validation

Use the versions declared in the project's build file as the source of truth.

## Authentication flow

1. **Register:** The client submits a username and password. The password is hashed with BCrypt before storage, and the new account receives the `USER` role.
2. **Login:** Spring Security authenticates the credentials. The API returns a signed access JWT and a random refresh token.
3. **Access a protected endpoint:** The client sends `Authorization: Bearer <access-token>`. The JWT filter verifies and parses the token, loads the user, and sets the request's security context.
4. **Refresh:** The client submits the refresh token. The service checks its stored hash, status, and expiry, then issues a replacement refresh token and a new access JWT. The previous token is marked `ROTATED`.
5. **Logout:** The supplied active refresh token is marked `REVOKED`.
6. **Logout all:** Active refresh tokens belonging to the authenticated user are revoked.

## API endpoints

All request and response examples below are illustrative. Confirm exact DTO fields and response formats in the source code.

| Method | Endpoint | Authentication | Purpose |
|---|---|---|---|
| `POST` | `/register` | Public | Register a user |
| `POST` | `/login` | Public | Authenticate and issue tokens |
| `GET` | `/wish` | Required | Example protected endpoint |
| `POST` | `/refresh` | Current security configuration requires authentication | Rotate refresh token and issue a new access token |
| `POST` | `/logout` | Current security configuration requires authentication | Revoke the supplied refresh token |
| `POST` | `/logout-all` | Required | Revoke active refresh tokens for the current user |

**Important:** In the current security configuration, only `/register` and `/login` are explicitly public. Therefore `/refresh` and `/logout` also require an access JWT unless the security rules are deliberately changed. Decide whether this is the intended API design before release; refresh and logout endpoints commonly need to remain usable when the access token has expired.

### Example requests

#### Register

```http
POST /register
Content-Type: application/json
```

```json
{
  "username": "sampleuser",
  "password": "ChangeMe123!"
}
```

#### Login

```http
POST /login
Content-Type: application/json
```

```json
{
  "username": "sampleuser",
  "password": "ChangeMe123!",
  "deviceId": "browser-session-1",
  "deviceName": "My browser"
}
```

The login response contains the access token and refresh token, along with username/message fields as defined by the response DTO.

#### Call a protected endpoint

```http
GET /wish
Authorization: Bearer <access-token>
```

#### Refresh tokens

```http
POST /refresh
Content-Type: application/json
Authorization: Bearer <access-token>
```

```json
{
  "refreshToken": "<refresh-token>"
}
```

Use the newly returned refresh token for the next refresh operation. Do not continue using the old rotated token.

#### Logout

```http
POST /logout
Content-Type: application/json
Authorization: Bearer <access-token>
```

```json
{
  "refreshToken": "<refresh-token>"
}
```

#### Logout from all devices

```http
POST /logout-all
Authorization: Bearer <access-token>
```

## Configuration

Configure the database and JWT properties in `src/main/resources/application.properties`. Example keys:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/authDb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=300000
jwt.refresh-token-expiration=604800000
```

The example values correspond to a 5-minute access-token lifetime and a 7-day refresh-token lifetime, in milliseconds. Keep actual credentials and signing secrets outside version control. For HS256, use a cryptographically random secret of at least 32 bytes. If a real secret has already been committed publicly, rotate it.

Ensure the MySQL database exists and that your local environment variables are set before starting the application. Use the project's existing schema-generation settings, if configured, to manage tables.

## Refresh-token storage design

The refresh-token entity stores fields such as:

- A unique hash of the raw refresh token
- User relationship
- Device ID and device name
- Creation and expiry timestamps
- Status (`ACTIVE`, `ROTATED`, or `REVOKED`)
- Last-used timestamp
- Hash of the replacement token, when rotated

The raw refresh token is returned to the client and should not be stored in the database. Device metadata is supplied by the client and is descriptive only; it is not proof of device identity.

## Security notes and known limitations

This is a learning project, not a production security certification.

- **Refresh-token reuse handling:** Reuse detection and family-wide revocation still need careful transaction-safe implementation. Do not assume that detection automatically revokes every session.
- **Concurrency:** The repository uses a pessimistic row lock for token lookup. Confirm that the entire rotation flow runs in the same transaction and that concurrent requests cannot rotate the same token more than once.
- **Logout behavior:** Revoking a refresh token does not invalidate an already-issued access JWT. That JWT may remain usable until it expires unless access-token revocation or another invalidation mechanism is added.
- **Endpoint access rules:** Review whether `/refresh` and `/logout` should require a still-valid access JWT.
- **JWT filter:** Parsing errors should return `401 Unauthorized`; verify behavior for missing users and other authentication failures as part of the test plan.
- **Validation and error handling:** Ensure invalid DTO fields, duplicate usernames, invalid credentials, and invalid refresh tokens return suitable HTTP status codes.
- **Secret management:** Never publish a real signing secret or database password.
- **HTTPS:** Use HTTPS when deployed so credentials and tokens are protected in transit.
- **Rate limiting and monitoring:** Add appropriate login/refresh rate limits, security logging without token values, and operational monitoring before production use.

## Testing plan

Testing is intentionally postponed until implementation is complete. The planned verification includes:

- Registration succeeds; passwords are stored as BCrypt hashes
- Duplicate username is rejected
- Login succeeds with valid credentials and fails with invalid credentials
- Protected endpoint accepts a valid access token
- Missing, malformed, expired, and invalid-signature JWTs are rejected
- Refresh succeeds once and rotates the old token
- Expired, revoked, malformed, and reused refresh tokens are rejected
- Concurrent refresh attempts cannot both succeed
- Logout revokes the intended refresh token
- Logout-all revokes the user's active refresh tokens
- Validation failures return `400 Bad Request`
- Authentication failures return appropriate `401 Unauthorized` responses

## Running locally

1. Install a compatible JDK and MySQL.
2. Create the `authDb` database (or update the configured database name).
3. Set `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in your local environment.
4. Confirm dependency versions and configuration in the build file and `application.properties`.
5. Start the Spring Boot application using your IDE or the build tool configured in the repository.
6. Complete the test plan above before considering the implementation finished.

## Learning outcomes

- How Spring Security's `AuthenticationManager` and `UserDetailsService` work together
- How to issue and validate signed JWT access tokens
- Why access tokens should be short-lived
- Why refresh tokens should be random, stored as hashes, and rotated
- How database transactions and row locks relate to concurrent refresh requests
- How logout and refresh-token revocation differ from access-token invalidation
- How to structure DTO validation and centralized exception handling

---

Built as a hands-on authentication and security engineering project.
