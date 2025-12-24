import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError, throwError } from 'rxjs';
import { AxiosError } from 'axios';
import { JwtService } from '@nestjs/jwt';
//import { UserUpdateDTO } from './dto/user-update.dto';

/**
 * Service for user profile operations.
 *
 * Handles communication with the User microservice for profile retrieval,
 * updates, search, and pagination. Implements circuit breaker pattern with
 * retry logic and timeout handling for resilience in service-to-service
 * communication.
 */
@Injectable()
export class ProfileService {

    /** Logger instance for structured logging */
    private readonly logger = new Logger(ProfileService.name);

    /** User microservice base URL */
    private readonly apiGatewayUrl: string;

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
        this.apiGatewayUrl = this.configService.get<string>('GATEWAY_URL', 'http://localhost:8080');
        this.requestTimeout = this.configService.get<number>('REQUEST_TIMEOUT', 5000);
        this.maxRetries = this.configService.get<number>('MAX_RETRIES', 3);
    }

    /**
     * Retrieves user by account ID from the User microservice.
     *
     * @param accountId Account identifier
     * @returns User information
     * @throws HttpException If user not found or service is unavailable
     */
    async getProfileData(token: string) {
        const pureToken = token.startsWith('Bearer ')
            ? token.substring(7)
            : token;

        const accountId = this.jwtService.decode(pureToken)?.sub;

        this.logger.log(`event=get_profile_data_request account_id=${accountId}`);

        const headers = { Authorization: token };

        try {
            const [accountResponse, userResponse] = await Promise.all([

                firstValueFrom(
                    this.httpService.get(
                        `${this.apiGatewayUrl}/api/accounts/me`,
                        { headers },
                    ).pipe(
                        timeout({ each: this.requestTimeout }),
                        retry(this.maxRetries),
                        catchError((e: AxiosError) =>
                            throwError(() => this.handleServiceError(e, 'accounts/me')),
                        ),
                    ),
                ),

                firstValueFrom(
                    this.httpService.get(
                        `${this.apiGatewayUrl}/api/users/me`,
                        { headers },
                    ).pipe(
                        timeout({ each: this.requestTimeout }),
                        retry(this.maxRetries),
                        catchError((e: AxiosError) =>
                            throwError(() => this.handleServiceError(e, 'users/me')),
                        ),
                    ),
                ),
            ]);

            this.logger.log(`event=get_profile_data_success account_id=${accountId}`);

            return {
                account: accountResponse.data,
                user: userResponse.data
            };

        } catch (error) {
            this.logger.error(`event=get_profile_data_fail account_id=${accountId} error=${error.message}`);
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