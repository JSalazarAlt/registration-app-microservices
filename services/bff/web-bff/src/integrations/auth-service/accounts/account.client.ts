import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';

import { AccountUpdateRequest } from '../../modules/accounts/dto/account-update.dto';
import { AccountMapper } from './account.mapper';

@Injectable()
export class AccountClient {

    private readonly logger = new Logger(AccountClient.name);

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
    async getAuthenticatedAccount(token: string) {
        const request = AccountMapper.toRegisterRequest(dto);
        const data = await this.post(
            '/api/accounts/me',
            request,
            'register'
        );
        return AccountMapper.toAuthTokensResponse(data);
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