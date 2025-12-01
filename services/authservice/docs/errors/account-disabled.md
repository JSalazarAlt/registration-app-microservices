# Account Disabled

**HTTP Status Code:** 403 Forbidden

**Error Code:** `ACCOUNT_DISABLED`

## Description

This error occurs when attempting to access an account that has been disabled by an administrator.

## Error Message

```
Account has been disabled by administrator
```

## Common Causes

- Account was disabled due to policy violations
- Account was disabled for security reasons
- Administrative action was taken to disable the account

## Resolution

- Contact system administrator to request account reactivation
- Review terms of service for potential violations

## Related Endpoints

- `POST /api/v1/auth/login`
- `GET /api/v1/accounts/me`