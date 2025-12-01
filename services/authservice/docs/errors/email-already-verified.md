# Email Already Verified

**HTTP Status Code:** 400 Bad Request

**Error Code:** `EMAIL_ALREADY_VERIFIED`

## Description

This error occurs when attempting to verify an email address that has already been verified.

## Error Message

```
Email '{email}' is already verified
```

## Common Causes

- Email verification token was already used
- Account email is already verified
- Attempting to verify email multiple times

## Resolution

- No action needed, email is already verified
- Proceed to login with your credentials

## Related Endpoints

- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/resend-verification`