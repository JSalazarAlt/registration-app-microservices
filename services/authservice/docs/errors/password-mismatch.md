# Password Mismatch

**HTTP Status Code:** 400 Bad Request

**Error Code:** `PASSWORD_MISMATCH`

## Description

This error occurs when the password and confirmation password fields do not match.

## Error Message

```
Password and confirmation password do not match
```

## Common Causes

- Password and confirmation password are different
- Typo in one of the password fields
- Copy-paste error

## Resolution

- Ensure both password fields contain the exact same value
- Re-type both password fields carefully
- Check for extra spaces or hidden characters

## Related Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/reset-password`