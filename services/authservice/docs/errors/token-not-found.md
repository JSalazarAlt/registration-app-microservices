# Token Not Found

**HTTP Status Code:** 404 Not Found

**Error Code:** `TOKEN_NOT_FOUND`

## Description

This error occurs when attempting to use a token that does not exist in the system.

## Error Message

```
Token not found with value={value}
```

## Common Causes

- Token was never issued
- Token was deleted from the system
- Token value is incorrect
- Token was already revoked

## Resolution

- Request a new token
- Verify the token value is correct
- For refresh tokens: Login again
- For verification tokens: Request new verification email

## Related Endpoints

- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/logout`