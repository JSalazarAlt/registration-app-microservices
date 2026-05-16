// ----------------------------------------------------------------
// TRADITIONAL REGISTRATION AND LOGIN
// ----------------------------------------------------------------

export interface RegistrationRequest {
    
    username: string;
    email: string;
    password: string;

    firstName: string;
    lastName: string;
    phoneNumber: string;
    profilePictureUrl: string;
    locale: string;
    timezone: string;

}

export interface AuthenticationRequest {

    identifier: string;
    password: string;

}

// ----------------------------------------------------------------
// GOOGLE OAUTH2 REGISTRATION AND LOGIN
// ----------------------------------------------------------------

export interface OAuth2AuthenticationRequest {

        email: string;
        name: string;
        provider: string;
        providerId: string;
        deviceName: string;

}

export interface AuthenticationResponse {
    
    accountId: string;
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    accessTokenExpiresIn: number;

}

// ----------------------------------------------------------------
// LOGOUT
// ----------------------------------------------------------------

export interface DeauthenticationRequest {

    /** Value of refresh token */
    value: string;

}

// ----------------------------------------------------------------
// TOKEN REFRESH
// ----------------------------------------------------------------

export interface RefreshTokenRequest {

    /** Value of refresh token */
    value: string;

}

// ----------------------------------------------------------------
// EMAIL MANAGEMENT
// ----------------------------------------------------------------

export interface EmailVerificationRequest {

    /** Value of email verification token */
    value: string;
    
}

// ----------------------------------------------------------------
// PASSWORD  MANAGEMENT
// ----------------------------------------------------------------