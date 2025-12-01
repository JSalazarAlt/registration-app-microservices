# Account Deleted

**HTTP Status Code:** 403 Forbidden

**Error Code:** `ACCOUNT_DELETED`

## Description

This error occurs when attempting to access an account that has been soft-deleted from the system.

## Error Message

```
Account has been deleted. Login to restore it
```

## Common Causes

- Account was deleted by the user
- Account was soft-deleted but not permanently removed from the database

## Resolution

- Login to the account to restore it from deleted state
- Contact support if unable to restore the account

## Related Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`