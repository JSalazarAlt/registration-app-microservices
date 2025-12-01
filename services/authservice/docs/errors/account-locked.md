# Account Locked

**HTTP Status Code:** 423 Locked

**Error Code:** `ACCOUNT_LOCKED`

## Description

This error occurs when an account is temporarily locked due to multiple failed login attempts.

## Error Message

```
Account is locked due to multiple failed login attempts. Try again after {unlockTime} minutes
```

## Common Causes

- 5 or more consecutive failed login attempts
- Security measure to prevent brute force attacks

## Resolution

- Wait for the lockout period to expire (24 hours from lock time)
- Account will automatically unlock after the specified time
- Contact support if you believe this is an error

## Related Endpoints

- `POST /api/v1/auth/login`

## Security Details

- Lockout Duration: 24 hours
- Failed Attempts Threshold: 5 attempts
- Auto-unlock: Yes, after lockout period expires