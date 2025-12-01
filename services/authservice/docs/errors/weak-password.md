# Weak Password

**HTTP Status Code:** 400 Bad Request

**Error Code:** `WEAK_PASSWORD`

## Description

This error occurs when the provided password does not meet the minimum security requirements.

## Error Message

```
Password does not meet security requirements. Must be at least 8 characters with uppercase, lowercase, and numbers
```

## Common Causes

- Password is too short (less than 8 characters)
- Password missing uppercase letters
- Password missing lowercase letters
- Password missing numbers
- Password is too simple or common

## Resolution

Create a password that meets all requirements:
- At least 8 characters long
- Contains at least one uppercase letter (A-Z)
- Contains at least one lowercase letter (a-z)
- Contains at least one number (0-9)

## Related Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/reset-password`
- `PATCH /api/v1/accounts/me`

## Password Best Practices

- Use a mix of characters, numbers, and symbols
- Avoid common words or patterns
- Don't reuse passwords from other accounts
- Consider using a password manager
- Make it at least 12 characters for better security