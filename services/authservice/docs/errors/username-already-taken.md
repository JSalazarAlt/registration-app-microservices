# Username Already Taken

**HTTP Status Code:** 409 Conflict

**Error Code:** `USERNAME_ALREADY_TAKEN`

## Description

This error occurs when attempting to register or update to a username that is already in use.

## Error Message

```
Username '{username}' is already taken
```

## Common Causes

- Username is already registered by another user
- Attempting to create duplicate account
- Username was previously used

## Resolution

- Choose a different username
- Add numbers or special characters to make it unique
- Try variations of your desired username

## Related Endpoints

- `POST /api/v1/auth/register`
- `PATCH /api/v1/accounts/me`