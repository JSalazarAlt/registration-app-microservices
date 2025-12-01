# OAuth2 Authentication Failed

**HTTP Status Code:** 401 Unauthorized

**Error Code:** `OAUTH2_AUTHENTICATION_FAILED`

## Description

This error occurs when OAuth2 authentication with an external provider fails.

## Error Message

```
OAuth2 authentication with {provider} failed
```

## Common Causes

- User denied permission to the application
- OAuth2 provider rejected the authentication request
- Invalid OAuth2 credentials or configuration
- Network issues with OAuth2 provider
- OAuth2 token exchange failed

## Resolution

- Try authenticating again
- Ensure you grant necessary permissions
- Check if OAuth2 provider service is available
- Contact support if issue persists

## Related Endpoints

- `POST /api/v1/auth/oauth2/google`

## Supported Providers

- Google OAuth2