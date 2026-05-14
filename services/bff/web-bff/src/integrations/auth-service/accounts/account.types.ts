export interface RegistrationRequest {
    
    // ----------------------------------------------------------------
    // ACCOUNT'S CREDENTIALS
    // ----------------------------------------------------------------
    
    username: string;
    email: string;
    password: string;

    // ----------------------------------------------------------------
    // USER'S PROFILE
    // ----------------------------------------------------------------

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

export interface AuthenticationResponse {
    
    accountId: string;
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    accessTokenExpiresIn: number;

}

export interface DeauthenticationRequest {

    /** Value of refresh token */
    value: string;

}

export interface RefreshTokenRequest {

    /** Value of refresh token */
    value: string;

}

export interface EmailVerificationRequest {

    /** Value of email verification token */
    value: string;
    
}