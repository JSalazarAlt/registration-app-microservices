import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';
import { LoginDTO } from './dto/login.dto';
import { RegistrationDTO } from './dto/registration.dto';
import { RefreshTokenDTO } from './dto/refresh-token.dto';

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

    /** Logger instance for structured logging */
    private readonly logger = new Logger(AuthService.name);

    /** Auth microservice base URL */
    private readonly authServiceUrl: string;

    /** HTTP request timeout in milliseconds */
    private readonly requestTimeout: number;

    /** Maximum retry attempts for failed requests */
    private readonly maxRetries: number;

    /**
     * Constructs AuthService with HTTP client and configuration.
     *
     * @param httpService HTTP client for making requests
     * @param configService Configuration service for environment variables
     */
    constructor(
        private readonly httpService: HttpService,
        private readonly configService: ConfigService,
    ) {
        this.authServiceUrl = this.configService.get<string>('AUTH_SERVICE_URL', 'http://localhost:8080');
        this.requestTimeout = this.configService.get<number>('REQUEST_TIMEOUT', 5000);
        this.maxRetries = this.configService.get<number>('MAX_RETRIES', 3);
    }

    /**
     * Forwards a registration request to the Auth microservice.
     *
     * Sends the account's information and user's profile to register a new
     * account and returns the created account's information.
     * 
     * @param registerData Account's information and user's profile
     * @returns Created account's information
     * @throws HttpException If registration fails or service is unavailable
     */
    async register(registerData: RegistrationDTO) {
        // Log registration request
        this.logger.log(`event=registration_request email=${registerData.email}`);

        try {
            // Send registration request to Auth microservice to retrieve
            // created account's information
            const response = await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/register`, registerData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'register');
                    }),
                ),
            );

            // Log registration success
            this.logger.log(`event=registration_success email=${registerData.email}`);

            // Return created account's information
            return response.data;
        } catch (error) {
            // Log registration failed
            this.logger.error(`event=registration_failed email=${registerData.email} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards a login request to the Auth microservice.
     *
     * Sends login credentials and returns the access and refresh tokens
     * issued by the Auth microservice.
     *
     * @param loginData Login credentials (username/email and password)
     * @returns Refresh and access tokens
     * @throws HttpException If authentication fails or service is unavailable
     */
    async login(loginData: LoginDTO) {
        // Log login request
        this.logger.log(`event=login_request identifier=${loginData.identifier}`);
        
        try {
            // Send login request to Auth microservice and retrieve tokens
            const response = await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/login`, loginData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'login');
                    }),
                ),
            );
            
            // Log login success
            this.logger.log(`event=login_success identifier=${loginData.identifier}`);

            // Return refresh and access tokens
            return response.data;
        } catch (error) {
            // Log login failed
            this.logger.error(`event=login_failed identifier=${loginData.identifier} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards a logout request to the Auth microservice.
     *
     * Sends a refresh token and returns a new refresh and access tokens.
     *
     * @param logoutData Refresh token value linked to account
     * @returns Logout confirmation message
     * @throws HttpException If communication with Auth service fails
     */
    async logout(logoutData: RefreshTokenDTO) {
        // Log logout request
        this.logger.log(`event=logout_request`);

        try {
            // Send logout request to Auth microservice
            await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/logout`, logoutData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'logout');
                    }),
                ),
            );
            // Log logout success
            this.logger.log(`event=logout_success`);

            // Return logout confirmation
            return {
                statusCode: 204,
                message: 'Logout successful'
            };
        } catch (error) {
            // Log logout failed
            this.logger.error(`event=logout_failed error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards a refresh token request to the Auth microservice.
     *
     * Sends a refresh token to be revoked.
     *
     * @param refreshData Refresh token value linked to account
     * @returns New refresh and access tokens
     * @throws HttpException If token refresh fails or service is unavailable
     */
    async refreshToken(refreshData: RefreshTokenDTO) {
        // Log refresh token request
        this.logger.log(`event=refresh_token_request`);

        try {
            // Send refresh token request to Auth microservice and retrieve tokens
            const response = await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/refresh`, refreshData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'refreshToken');
                    }),
                ),
            );

            // Log refresh token success
            this.logger.log(`event=refresh_token_success`);

            // Return new refresh and access tokens
            return response.data;
        } catch (error) {
            // Log refresh token failed
            this.logger.error(`event=refresh_token_failed error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards an email verification token request to the Auth microservice.
     *
     * Sends an email verification token to verify the email linked to an
     * account.
     *
     * @param verifyData Email verification token value linked to account
     * @returns Updated account's information
     * @throws HttpException If token refresh fails or service is unavailable
     */
    async verifyEmail(verifyData: RefreshTokenDTO) {
        // Log refresh token request
        this.logger.log(`event=verify_email_request`)

        try {
            
            const response = await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/verify-email`, verifyData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'verifyEmail');
                    }),
                ),
            );

            // Log verify email success
            this.logger.log(`event=verify_email_success`);

            // Return updated account's information
            return response.data;
        } catch (error) {
            // Log verify email failed
            this.logger.error(`event=refresh_token_failed error=${error.message}`);
            throw error;
        }
    }

    /**
     * Handles errors from Auth microservice communication.
     *
     * Transforms Axios errors into appropriate HTTP exceptions with error
     * messages and status codes. Handles network errors, timeouts, and
     * service unavailability.
     *
     * @param error Axios error from HTTP request
     * @param operation Operation name for logging context
     * @returns HttpException with appropriate status and message
     */
    private handleServiceError(error: AxiosError, operation: string): HttpException {
        if (error.response) {
            // Auth service returned an error response
            const status = error.response.status;
            const message = error.response.data || 'Auth service error';
            this.logger.error(`event=auth_service_error operation=${operation} status=${status} message=${JSON.stringify(message)}`);
            return new HttpException(message, status);
        } else if (error.code === 'ECONNREFUSED') {
            // Auth service is unavailable
            this.logger.error(`event=auth_service_unavailable operation=${operation}`);
            return new HttpException('Auth service is currently unavailable', HttpStatus.SERVICE_UNAVAILABLE);
        } else if (error.name === 'TimeoutError') {
            // Request timed out
            this.logger.error(`event=auth_service_timeout operation=${operation}`);
            return new HttpException('Auth service request timed out', HttpStatus.GATEWAY_TIMEOUT);
        } else {
            // Unknown error
            this.logger.error(`event=auth_service_unknown_error operation=${operation} error=${error.message}`);
            return new HttpException('Failed to communicate with Auth service', HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}