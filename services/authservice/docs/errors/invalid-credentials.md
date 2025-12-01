# Invalid Credentials

**HTTP Status Code:** 401 Unauthorized

**Error Code:** `INVALID_CREDENTIALS`

## Description

This error occurs when attempting to login with incorrect email/username or password.

## Error Message

```
Invalid email/username or password
```

## Common Causes

- Incorrect password
- Incorrect email or username
- Typo in credentials
- Account does not exist

## Resolution

- Verify your email/username and password
- Check for typos and correct capitalization
- Use password reset if you forgot your password
- Register a new account if you don't have one

## Related Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/reset-password`

## Security Note

Multiple failed login attempts (5+) will result in account lockout for 24 hours.