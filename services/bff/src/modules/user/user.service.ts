import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';
import { JwtService } from '@nestjs/jwt';
import { UserUpdateDTO } from './dto/user-update.dto';

/**
 * Service for user profile operations.
 *
 * Handles communication with the User microservice for profile retrieval,
 * updates, search, and pagination. Implements circuit breaker pattern with
 * retry logic and timeout handling for resilience in service-to-service
 * communication.
 *
 * @author Joel Salazar
 */
@Injectable()
export class UserService {

    /** Logger instance for structured logging */
    private readonly logger = new Logger(UserService.name);

    /** User microservice base URL */
    private readonly userServiceUrl: string;

    /** HTTP request timeout in milliseconds */
    private readonly requestTimeout: number;

    /** Maximum retry attempts for failed requests */
    private readonly maxRetries: number;

    /**
     * Constructs UserService with HTTP client and configuration.
     *
     * @param httpService HTTP client for making requests
     * @param configService Configuration service for environment variables
     */
    constructor(
        private readonly httpService: HttpService,
        private readonly configService: ConfigService,
        private readonly jwtService: JwtService
    ) {
        this.userServiceUrl = this.configService.get<string>('USER_SERVICE_URL', 'http://localhost:8081');
        this.requestTimeout = this.configService.get<number>('REQUEST_TIMEOUT', 5000);
        this.maxRetries = this.configService.get<number>('MAX_RETRIES', 3);
    }

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Retrieves a paginated list of all users from User microservice.
     *
     * @param page Page number (zero-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @returns Paginated list of users' profiles
     * @throws HttpException If retrieval fails or service is unavailable
     */
    async getAllUsers(page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') {
        // Log request to get all users
        this.logger.log(`event=get_all_users_request page=${page} size=${size}`);

        try {
            // Send request to User microservice to retrieve users' profiles
            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users`, {
                params: { page, size, sortBy, sortDir },
                }).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'getAllUsers');
                    }),
                ),
            );

            // Log success to get all users
            this.logger.log(`event=get_all_users_success page=${page} size=${size}`);

            // Return paginated list of users' profiles
            return response.data;
        } catch (error) {
            // Log fail to get all users and throw error
            this.logger.error(`event=get_all_users_fail page=${page} size=${size} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Searches users by name from the User microservice.
     *
     * Performs case-insensitive search on user first and last names.
     *
     * @param name Search query for user name
     * @returns List of matching users
     * @throws HttpException If search fails or service is unavailable
     */
    async searchUsers(name: string) {
        try {
            this.logger.log(`event=search_users_request name=${name}`);

            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users/search`, {
                params: { name },
                }).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'searchUsers');
                    }),
                ),
            );

            this.logger.log(`event=search_users_success name=${name}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=search_users_failed name=${name} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Retrieves user by ID from the User microservice.
     *
     * @param userId User's ID
     * @returns User's profile
     * @throws HttpException If user not found or service is unavailable
     */
    async getUserById(userId: string) {
        // Log request to get user by ID
        this.logger.log(`event=get_user_by_id_request user_id=${userId}`);
        
        try {
            // Send request to User microservice to retrieve user's profile
            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users/${userId}`).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'getUserById');
                    }),
                ),
            );

            // Log success to get user by ID
            this.logger.log(`event=get_user_by_id_success user_id=${userId}`);

            // Return user's profile
            return response.data;
        } catch (error) {
            // Log fail to get user by ID and throw error
            this.logger.error(`event=get_user_by_id_failed user_id=${userId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Updates user by user ID with the User microservice.
     *
     * @param userId User identifier
     * @param updateData User update data
     * @returns Updated user information
     * @throws HttpException If update fails or service is unavailable
     */
    async updateUserById(userId: string, updateData: UserUpdateDTO) {
        // Log request to update user by ID
        this.logger.log(`event=update_user_by_id_request user_id=${userId}`);

        try {
            // Send request to User microservice to update user's profile
            const response = await firstValueFrom(
                this.httpService.put(`${this.userServiceUrl}/api/v1/users/${userId}`, updateData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'updateUserById');
                    }),
                ),
            );

            // Log success to update user by ID
            this.logger.log(`event=update_user_by_id_success user_id=${userId}`);

            // Return updated user's profile
            return response.data;
        } catch (error) {
            // Log fail to update user by ID and throw error
            this.logger.error(`event=update_user_by_id_failed user_id=${userId} error=${error.message}`);
            throw error;
        }
    }
    
    // ----------------------------------------------------------------
    // USER MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Retrieves user by account ID from the User microservice.
     *
     * @param accountId Account identifier
     * @returns User information
     * @throws HttpException If user not found or service is unavailable
     */
    async getAuthenticatedUser(token: string) {

        const pureToken = token.startsWith('Bearer ')
            ? token.substring(7)
            : token;

        const accountId = this.jwtService.decode(pureToken)?.sub;

        // Log request to get authenticated user
        this.logger.log(`event=get_authenticated_user_request account_id=${accountId}`);

        try {
            // Send request to User microservice to retrieve authenticated user's profile
            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users/me`,
                    { headers: { Authorization: token } }
                ).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'getAuthenticatedUser');
                    }),
                ),
            );

            // Log success to get authenticated user
            this.logger.log(`event=get_authenticated_user_success account_id=${accountId}`);

            // Return authenticated user's profile
            return response.data;
        } catch (error) {
            // Log fail to get authenticated user and throw error
            this.logger.error(`event=get_authenticated_user_fail account_id=${accountId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Updates user by account ID with the User microservice.
     *
     * @param accountId Account identifier
     * @param updateData User update data
     * @returns Updated user information
     * @throws HttpException If update fails or service is unavailable
     */
    async updateAuthenticatedUser(token: string, updateData: UserUpdateDTO) {

        const pureToken = token.startsWith('Bearer ')
            ? token.substring(7)
            : token;

        const accountId = this.jwtService.decode(pureToken)?.sub;

        // Log request to get authenticated user
        this.logger.log(`event=update_authenticated_user_request account_id=${accountId}`);
        
        try {
            // Send request to User microservice to update authenticated user's profile
            const response = await firstValueFrom(
                this.httpService.put(`${this.userServiceUrl}/api/v1/users/me`, 
                    updateData, 
                    { headers: { Authorization: token } }
                ).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'updateAuthenticatedUser');
                    }),
                ),
            );

            // Log success to get authenticated user
            this.logger.log(`event=update_authenticated_user_success account_id=${accountId}`);

            // Return updated authenticated user's profile
            return response.data;
        } catch (error) {
            // Log fail to get authenticated user
            this.logger.error(`event=update_authenticated_user_fail account_id=${accountId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Handles errors from User microservice communication.
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
            // User service returned an error response
            const status = error.response.status;
            const message = error.response.data || 'User service error';
            this.logger.error(`event=user_service_error operation=${operation} status=${status} message=${JSON.stringify(message)}`);
            return new HttpException(message, status);
        } else if (error.code === 'ECONNREFUSED') {
            // User service is unavailable
            this.logger.error(`event=user_service_unavailable operation=${operation}`);
            return new HttpException('User service is currently unavailable', HttpStatus.SERVICE_UNAVAILABLE);
        } else if (error.name === 'TimeoutError') {
            // Request timed out
            this.logger.error(`event=user_service_timeout operation=${operation}`);
            return new HttpException('User service request timed out', HttpStatus.GATEWAY_TIMEOUT);
        } else {
            // Unknown error
            this.logger.error(`event=user_service_unknown_error operation=${operation} error=${error.message}`);
            return new HttpException('Failed to communicate with User service', HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}