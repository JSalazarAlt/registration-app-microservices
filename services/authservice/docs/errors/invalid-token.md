# Invalid Token

**HTTP Status Code:** 410 Gone

**Error Code:** `INVALID_TOKEN`

## Description

This error occurs when attempting to use a token that is invalid, expired, or revoked.

## Error Message

```
Invalid {token_type} token
```

Token types include:
- refresh token
- email verification token
- password reset token

## Common Causes

- Token has expired
- Token was already used
- Token was revoked
- Token format is invalid
- Token does not exist

## Resolution

- Request a new token
- For refresh tokens: Login again to get new tokens
- For email verification: Request new verification email
- For password reset: Request new password reset link

## Related Endpoints

- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/resend-verification`
- `POST /api/v1/auth/reset-password`

## Token Lifetimes

- Access Token: 15 minutes
- Refresh Token: 7 days
- Email Verification Token: 24 hours
- Password Reset Token: 1 hour