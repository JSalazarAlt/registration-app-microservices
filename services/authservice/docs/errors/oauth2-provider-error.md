# OAuth2 Provider Error

**HTTP Status Code:** 502 Bad Gateway

**Error Code:** `OAUTH2_PROVIDER_ERROR`

## Description

This error occurs when the OAuth2 provider returns an error or is unavailable.

## Error Message

```
OAuth2 provider {provider} returned an error
```

## Common Causes

- OAuth2 provider service is down
- OAuth2 provider API returned an error
- Network connectivity issues
- OAuth2 provider rate limiting
- Invalid OAuth2 configuration

## Resolution

- Wait a few moments and try again
- Check OAuth2 provider status page
- Verify network connectivity
- Contact support if issue persists

## Related Endpoints

- `POST /api/v1/auth/oauth2/google`

## Supported Providers

- Google OAuth2