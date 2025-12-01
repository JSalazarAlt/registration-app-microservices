# Email Not Verified

**HTTP Status Code:** 403 Forbidden

**Error Code:** `EMAIL_NOT_VERIFIED`

## Description

This error occurs when attempting to login with an account whose email address has not been verified.

## Error Message

```
Email '{email}' has not been verified. Please check your inbox
```

## Common Causes

- Email verification link was not clicked
- Email verification token expired
- Verification email was not received

## Resolution

- Check your email inbox for verification link
- Check spam/junk folder
- Request a new verification email
- Click the verification link in the email

## Related Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/resend-verification`
- `POST /api/v1/auth/verify-email`