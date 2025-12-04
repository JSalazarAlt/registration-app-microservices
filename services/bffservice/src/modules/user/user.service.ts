import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';

/**
 * Service for user profile operations.
 *
 * <p>Handles communication with the User microservice for profile retrieval,
 * updates, search, and pagination. Implements circuit breaker pattern with
 * retry logic and timeout handling for resilient service-to-service
 * communication.</p>
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
    ) {
        this.userServiceUrl = this.configService.get<string>('USER_SERVICE_URL', 'http://localhost:8081');
        this.requestTimeout = this.configService.get<number>('REQUEST_TIMEOUT', 5000);
        this.maxRetries = this.configService.get<number>('MAX_RETRIES', 3);
    }

    /**
     * Retrieves user by user ID from the User microservice.
     *
     * <p>Fetches user details including personal information,
     * preferences, and timestamps. Implements retry logic and timeout
     * handling for resilient communication.</p>
     *
     * @param userId User identifier
     * @returns User information
     * @throws HttpException If user not found or service is unavailable
     */
    async getUserById(userId: string) {
        try {
            this.logger.log(`event=get_user_by_id_request user_id=${userId}`);

            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users/${userId}`).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'getUserById');
                    }),
                ),
            );

            this.logger.log(`event=get_user_by_id_success user_id=${userId}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=get_user_by_id_failed user_id=${userId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Updates user by user ID with the User microservice.
     *
     * <p>Updates user fields including personal information,
     * preferences, and profile picture. Implements retry logic and timeout
     * handling for resilient communication.</p>
     *
     * @param userId User identifier
     * @param updateData User update data
     * @returns Updated user information
     * @throws HttpException If update fails or service is unavailable
     */
    async updateUserById(userId: string, updateData: any) {
        try {
            this.logger.log(`event=update_user_by_id_request user_id=${userId}`);

            const response = await firstValueFrom(
                this.httpService.put(`${this.userServiceUrl}/api/v1/users/${userId}`, updateData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'updateUserById');
                    }),
                ),
            );

            this.logger.log(`event=update_user_by_id_success user_id=${userId}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=update_user_by_id_failed user_id=${userId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Retrieves user by account ID from the User microservice.
     *
     * <p>Fetches user details by their associated account ID. Implements
     * retry logic and timeout handling for resilient communication.</p>
     *
     * @param accountId Account identifier
     * @returns User information
     * @throws HttpException If user not found or service is unavailable
     */
    async getUserByAccountId(accountId: string) {
        try {
            this.logger.log(`event=get_user_by_account_id_request account_id=${accountId}`);

            const response = await firstValueFrom(
                this.httpService.get(`${this.userServiceUrl}/api/v1/users/account/${accountId}`).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'getUserByAccountId');
                    }),
                ),
            );

            this.logger.log(`event=get_user_by_account_id_success account_id=${accountId}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=get_user_by_account_id_failed account_id=${accountId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Updates user by account ID with the User microservice.
     *
     * <p>Updates user fields by their associated account ID. Implements
     * retry logic and timeout handling for resilient communication.</p>
     *
     * @param accountId Account identifier
     * @param updateData User update data
     * @returns Updated user information
     * @throws HttpException If update fails or service is unavailable
     */
    async updateUserByAccountId(accountId: string, updateData: any) {
        try {
            this.logger.log(`event=update_user_by_account_id_request account_id=${accountId}`);

            const response = await firstValueFrom(
                this.httpService.put(`${this.userServiceUrl}/api/v1/users/account/${accountId}`, updateData).pipe(
                    timeout({ each: this.requestTimeout }),
                    retry(this.maxRetries),
                    catchError((error: AxiosError) => {
                        throw this.handleServiceError(error, 'updateUserByAccountId');
                    }),
                ),
            );

            this.logger.log(`event=update_user_by_account_id_success account_id=${accountId}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=update_user_by_account_id_failed account_id=${accountId} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Retrieves all users with pagination from the User microservice.
     *
     * <p>Fetches paginated list of users with sorting options. Implements
     * retry logic and timeout handling for resilient communication.</p>
     *
     * @param page Page number (zero-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @returns Paginated user list
     * @throws HttpException If retrieval fails or service is unavailable
     */
    async getAllUsers(page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') {
        try {
            this.logger.log(`event=get_all_users_request page=${page} size=${size}`);

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

            this.logger.log(`event=get_all_users_success page=${page} size=${size}`);
            return response.data;
        } catch (error) {
            this.logger.error(`event=get_all_users_failed page=${page} size=${size} error=${error.message}`);
            throw error;
        }
    }

    /**
     * Searches users by name from the User microservice.
     *
     * <p>Performs case-insensitive search on user first and last names.
     * Implements retry logic and timeout handling for resilient
     * communication.</p>
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