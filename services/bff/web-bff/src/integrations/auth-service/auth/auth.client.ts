import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';

import { LoginRequestDTO } from '../../../modules/auth/dto/request/login-request.dto';
import { RegistrationRequestDTO } from '../../../modules/auth/dto/request/registration-request.dto';
import { AuthMapper } from './auth.mapper';
import { OAuth2RequestDTO } from '../../../modules/auth/dto/request/oauth2-request.dto';

@Injectable()
export class AuthClient {

    private readonly logger = new Logger(AuthClient.name);

    private readonly baseUrl: string;
    private readonly requestTimeout: number;
    private readonly maxRetries: number;

    constructor(
        private readonly http: HttpService,
        private readonly config: ConfigService
    ) {
        this.baseUrl = this.config.get<string>(
            'AUTH_MICROSERVICE_URL',
            'http://localhost:8081'
        );

        this.requestTimeout = this.config.get<number>(
            'REQUEST_TIMEOUT',
            5000
        );

        this.maxRetries = this.config.get<number>(
            'MAX_RETRIES',
            3
        );
    }

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * 
     * @param dto 
     * @returns 
     */
    async register(dto: RegistrationRequestDTO) {
        const request = AuthMapper.toRegistrationRequest(dto);
        const data = await this.post(
            '/api/auth/register',
            request,
            'register'
        );
        return AuthMapper.toAuthTokensResponse(data);
    }

    /**
     * 
     * @param dto 
     * @returns 
     */
    async login(dto: LoginRequestDTO) {
        const request = AuthMapper.toAuthenticationRequest(dto);
        const data = await this.post(
            '/api/auth/login',
            request,
            'login'
        );
        return AuthMapper.toAuthTokensResponse(data);
    }

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * 
     * @param dto 
     * @returns 
     */
    async processGoogleOAuth2Account(dto: OAuth2RequestDTO) {
        const request = AuthMapper.toOAuth2AuthenticationRequest(dto);
        const data = await this.post(
            '/api/auth/login',
            request,
            'login'
        );
        return AuthMapper.toAuthTokensResponse(data);
    }

    // ------------------------------------------------
    // REFRESH TOKEN
    // ------------------------------------------------

    async refreshToken(refreshToken: string) {
        const request = AuthMapper.toRefreshRequest(refreshToken);

        const data = await this.post(
            '/api/auth/refresh',
            request,
            'refreshToken'
        );

        return AuthMapper.toAuthTokensResponse(data);
    }

    // ------------------------------------------------
    // VERIFY EMAIL
    // ------------------------------------------------

    async verifyEmail(token: string) {
        const request = AuthMapper.toVerifyEmailRequest(token);

        return this.post(
            '/api/auth/email/verify',
            request,
            'verifyEmail'
        );
    }

    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    async logout(
        accessToken: string,
        refreshToken: string,
    ) {
        return this.post(
            '/api/auth/logout',
            { value: refreshToken },
            'logout',
            { Authorization: accessToken }
        );
    }

    // ------------------------------------------------
    // SHARED POST HELPER
    // ------------------------------------------------

    private async post(
        path: string,
        body: any,
        operation: string,
        headers?: Record<string, string>,
    ) {
        try {
            const response = await firstValueFrom(
                this.http
                .post(
                    `${this.baseUrl}${path}`,
                    body,
                    { headers },
                )
                .pipe(
                    timeout({
                        each: this.requestTimeout,
                    }),

                    retry(this.maxRetries),

                    catchError((error: AxiosError) => {
                        throw this.handleError(
                            error,
                            operation,
                        );
                    }),
                ),
            );

            this.logger.log(
                `event=${operation}_success`,
            );

            return response.data;
        } catch (error: unknown) {
            const errorMessage = error instanceof Error ? error.message : 'Unknown error';
            this.logger.error(
                `event=${operation}_failed error=${errorMessage}`,
            );

            throw error;
        }
    }

    // ------------------------------------------------
    // ERROR HANDLER
    // ------------------------------------------------

    private handleError(
        error: AxiosError,
        operation: string,
    ): HttpException {
        if (error.response) {
            const status = error.response.status;

            const message =
                typeof error.response.data === 'string'
                ? error.response.data
                : (error.response.data as any)
                    ?.message ??
                    'Auth service error';

            return new HttpException(
                message,
                status,
            );
        }

        if (error.code === 'ECONNREFUSED') {
            return new HttpException(
                'Auth service unavailable',
                HttpStatus.SERVICE_UNAVAILABLE,
            );
        }

        if (error.name === 'TimeoutError') {
            return new HttpException(
                'Auth service timeout',
                HttpStatus.GATEWAY_TIMEOUT,
            );
        }

        return new HttpException(
            'Unknown auth service error',
            HttpStatus.INTERNAL_SERVER_ERROR,
        );
    }

}