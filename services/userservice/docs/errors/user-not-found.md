# User Not Found

**HTTP Status Code:** 404 Not Found

**Error Code:** `USER_NOT_FOUND`

## Description

This error occurs when attempting to access a user profile that does not exist in the system.

## Error Message

```
User not found with {detail}
```

## Common Causes

- User ID does not exist
- Account ID has no associated user profile
- User profile was deleted
- User profile was never created

## Resolution

- Verify the user identifier (ID or account ID)
- Ensure user profile was created during registration
- Check if user was deleted
- Contact support if issue persists

## Related Endpoints

- `GET /api/v1/users/{userId}`
- `GET /api/v1/users/account/{accountId}`
- `PUT /api/v1/users/{userId}`
- `PUT /api/v1/users/account/{accountId}`