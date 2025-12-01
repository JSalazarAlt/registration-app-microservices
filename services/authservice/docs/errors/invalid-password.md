# Invalid Password

**HTTP Status Code:** 400 Bad Request

**Error Code:** `INVALID_PASSWORD`

## Description

This error occurs when the provided password does not meet validation requirements or is incorrect.

## Error Message

```
Invalid password
```

## Common Causes

- Password does not meet security requirements
- Password format is incorrect
- Password validation failed

## Resolution

- Ensure password meets all security requirements
- Check password format and length
- Review password policy requirements

## Related Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/reset-password`
- `PATCH /api/v1/accounts/me`

## Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number