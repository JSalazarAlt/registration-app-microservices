import { Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class UserService {
  private readonly userServiceUrl: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.userServiceUrl = this.configService.get<string>('USER_SERVICE_URL', 'http://localhost:8081');
  }

  async getUserProfile(accountId: string) {
    const response = await firstValueFrom(
      this.httpService.get(`${this.userServiceUrl}/api/v1/users/account/${accountId}`)
    );
    return response.data;
  }

  async updateUserProfile(accountId: string, updateData: any) {
    const response = await firstValueFrom(
      this.httpService.put(`${this.userServiceUrl}/api/v1/users/account/${accountId}`, updateData)
    );
    return response.data;
  }

  async getAllUsers(page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') {
    const response = await firstValueFrom(
      this.httpService.get(`${this.userServiceUrl}/api/v1/users`, {
        params: { page, size, sortBy, sortDir }
      })
    );
    return response.data;
  }

  async searchUsers(name: string) {
    const response = await firstValueFrom(
      this.httpService.get(`${this.userServiceUrl}/api/v1/users/search`, {
        params: { name }
      })
    );
    return response.data;
  }
}