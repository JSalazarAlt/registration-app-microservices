# Email Already Registered

**HTTP Status Code:** 409 Conflict

**Error Code:** `EMAIL_ALREADY_REGISTERED`

## Description

This error occurs when attempting to register with an email address that is already associated with an existing account.

## Error Message

```
Email '{email}' is already registered
```

## Common Causes

- Email address is already in use by another account
- Attempting to create duplicate account
- Email was previously registered

## Resolution

- Use a different email address
- Login to existing account with this email
- Use password reset if you forgot your credentials
- Contact support if you believe this is an error

## Related Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`