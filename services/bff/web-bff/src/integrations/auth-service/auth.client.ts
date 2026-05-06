// integrations/auth-service/auth.client.ts

import { Injectable, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom, timeout, retry, catchError } from 'rxjs';
import { AxiosError } from 'axios';

import { LoginDTO } from '../../modules/auth/dto/login.dto';
import { RegistrationDTO } from '../../modules/auth/dto/registration.dto';

@Injectable()
export class AuthClient {
    private readonly logger = new Logger(AuthClient.name);
    private readonly baseUrl: string;
    private readonly requestTimeout: number;
    private readonly maxRetries: number;

    constructor(
        private readonly http: HttpService,
        private readonly config: ConfigService,
    ) {
        this.baseUrl = this.config.get<string>('AUTH_MICROSERVICE_URL');
        this.requestTimeout = this.config.get<number>('REQUEST_TIMEOUT', 5000);
        this.maxRetries = this.config.get<number>('MAX_RETRIES', 3);
    }

    async login(data: LoginDTO) {
        return this.post('/api/auth/login', data, 'login');
    }

    async register(data: RegistrationDTO) {
        return this.post('/api/auth/register', data, 'register');
    }

    async logout(accessToken: string, refreshToken: string) {
        return this.post(
            '/api/auth/logout',
            { value: refreshToken },
            'logout',
            { Authorization: accessToken },
        );
    }

    async refreshToken(refreshToken: string) {
        return this.post(
            '/api/auth/refresh',
            { value: refreshToken },
            'refreshToken',
        );
    }

    async verifyEmail(token: string) {
        return this.post(
            '/api/auth/email/verify',
            token,
            'verifyEmail',
        );
    }

  // CENTRALIZED REQUEST HANDLER
  private async post(
    path: string,
    body: any,
    operation: string,
    headers?: Record<string, string>,
  ) {
    try {
      const response = await firstValueFrom(
        this.http
          .post(`${this.baseUrl}${path}`, body, { headers })
          .pipe(
            timeout({ each: this.requestTimeout }),
            retry(this.maxRetries),
            catchError((error: AxiosError) => {
              throw this.handleError(error, operation);
            }),
          ),
      );

      return response.data;
    } catch (error) {
      this.logger.error(`event=${operation}_failed error=${error.message}`);
      throw error;
    }
  }

  private handleError(error: AxiosError, operation: string): HttpException {
    if (error.response) {
      const status = error.response.status;
      const message =
        typeof error.response.data === 'string'
          ? error.response.data
          : (error.response.data as any)?.message ?? 'Auth service error';

      return new HttpException(message, status);
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