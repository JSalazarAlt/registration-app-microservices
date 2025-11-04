import { Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class AuthService {
  private readonly authServiceUrl: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.authServiceUrl = this.configService.get<string>('AUTH_SERVICE_URL', 'http://localhost:8080');
  }

  async register(registerData: any) {
    const response = await firstValueFrom(
      this.httpService.post(`${this.authServiceUrl}/api/v1/auth/register`, registerData)
    );
    return response.data;
  }

  async login(loginData: any) {
    const response = await firstValueFrom(
      this.httpService.post(`${this.authServiceUrl}/api/v1/auth/login`, loginData)
    );
    return response.data;
  }

  async logout(logoutData: any) {
    const response = await firstValueFrom(
      this.httpService.post(`${this.authServiceUrl}/api/v1/auth/logout`, logoutData)
    );
    return response.data;
  }

  async refreshToken(refreshData: any) {
    const response = await firstValueFrom(
      this.httpService.post(`${this.authServiceUrl}/api/v1/auth/refresh`, refreshData)
    );
    return response.data;
  }

  async getAccountByUsername(username: string) {
    const response = await firstValueFrom(
      this.httpService.get(`${this.authServiceUrl}/api/v1/accounts/${username}`)
    );
    return response.data;
  }
}