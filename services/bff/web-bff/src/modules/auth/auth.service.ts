import { Injectable } from '@nestjs/common';

import { LoginDTO } from './dto/request/login-request.dto';
import { RegistrationDTO } from './dto/request/registration-request.dto';
import { AuthClient } from '../../integrations/auth-service/auth/auth.client';

/**
 * Service for authentication-related operations.
 *
 * Handles communication with the Auth microservice for registration, login,
 * logout, and refresh token operations. Implements circuit breaker patterns
 * with retry logic and timeout handling for resilience in service-to-service 
 * communication.
 */
@Injectable()
export class AuthService {

    constructor(private readonly authClient: AuthClient) {}

    login(dto: LoginDTO) {
        return this.authClient.login(dto);
    }

    register(dto: RegistrationDTO) {
        return this.authClient.register(dto);
    }

    refreshToken(token: string) {
        return this.authClient.refreshToken(token);
    }

    logout(accessToken: string, refreshToken: string) {
        return this.authClient.logout(accessToken, refreshToken);
    }

}