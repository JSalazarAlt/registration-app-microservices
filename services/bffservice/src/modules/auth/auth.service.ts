import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';
import { LoginDTO } from './dto/login.dto';
import { RegistrationDTO } from './dto/registration.dto';
import { RefreshTokenDTO } from './dto/refreshToken.dto';

/**
 * Service for authentication-related operations.
 *
 * <p>Handles communication with the Auth microservice for account creation,
 * authentication, logout, and token refresh operations. Implements circuit
 * breaker pattern with retry logic and timeout handling for resilient
 * service-to-service communication.</p>
 *
 * @author Joel Salazar
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
     * <p>Creates a new account if the username and email are not already
     * registered. Implements retry logic and timeout handling for resilient
     * communication.</p>
     * 
     * @param registerData Registration data containing account's credentials 
     * and user's profile
     * @returns Created account's information
     * @throws HttpException If registration fails or service is unavailable
     */
    async register(registerData: RegistrationDTO) {
        // Log registration request
        this.logger.log(`event=registration_request email=${registerData.email}`);

        try {
            // Send registration request to Auth microservice and retrieve
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

            // Log successful registration
            this.logger.log(`event=registration_success email=${registerData.email}`);

            // Return created account's information
            return response.data;
        } catch (error) {
            // Log failed registration
            this.logger.error(`event=registration_failed email=${registerData.email} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards a login request to the Auth microservice.
     *
     * <p>Verifies an account using login credentials and returns refresh and
     * access tokens on successful authentication. Implements retry logic and
     * timeout handling for resilient communication.</p>
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
            
            // Log successful login
            this.logger.log(`event=login_success identifier=${loginData.identifier}`);

            // Return refresh and access tokens
            return response.data;
        } catch (error) {
            // Log failed login
            this.logger.error(`event=login_failed identifier=${loginData.identifier} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Forwards a logout request to the Auth microservice.
     *
     * <p>Revokes the provided refresh token, effectively ending the user
     * session. Implements retry and timeout logic to ensure resilient
     * communication with downstream services.</p>
     *
     * @param logoutData Object containing the refresh token to revoke
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
            // Log successful logout
            this.logger.log(`event=logout_success`);

            // Return logout confirmation
            return {
                statusCode: 204,
                message: 'Logout successful'
            };
        } catch (error) {
            // Log failed logout
            this.logger.error(`event=logout_failed error=${error.message}`);
            throw error;
        }
    }

    /**
     * Refreshes JWT access token with the Auth microservice.
     *
     * <p>Issues a new access token using a valid refresh token. Implements
     * retry logic and timeout handling for resilient communication.</p>
     *
     * @param refreshData Refresh token value
     * @returns New access token
     * @throws HttpException If token refresh fails or service is unavailable
     */
    async refreshToken(refreshData: any) {
        try {
            this.logger.log(`event=refresh_token_request`);

            const response = await firstValueFrom(
                this.httpService.post(`${this.authServiceUrl}/api/v1/auth/refresh`, refreshData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'refreshToken');
                    }),
                ),
            );

            this.logger.log(`event=refresh_token_success`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=refresh_token_failed error=${error.message}`);
            throw error;
        }
    }

    /**
     * Handles errors from Auth microservice communication.
     *
     * <p>Transforms Axios errors into appropriate HTTP exceptions with
     * meaningful error messages and status codes. Handles network errors,
     * timeouts, and service unavailability.</p>
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