# Account Not Found

**HTTP Status Code:** 404 Not Found

**Error Code:** `ACCOUNT_NOT_FOUND`

## Description

This error occurs when attempting to access an account that does not exist in the system.

## Error Message

```
Account not found with {detail}
```

## Common Causes

- Account ID does not exist
- Username does not exist
- Email address is not registered
- Account was permanently deleted

## Resolution

- Verify the account identifier (ID, username, or email)
- Register a new account if needed
- Check for typos in the identifier

## Related Endpoints

- `GET /api/v1/accounts/{username}`
- `GET /api/v1/accounts/me`
- `POST /api/v1/auth/login`