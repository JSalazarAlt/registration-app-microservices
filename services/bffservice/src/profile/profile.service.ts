import { Injectable } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class ProfileService {
  private readonly authServiceUrl: string;
  private readonly userServiceUrl: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.authServiceUrl = this.configService.get<string>('AUTH_SERVICE_URL', 'http://localhost:8080');
    this.userServiceUrl = this.configService.get<string>('USER_SERVICE_URL', 'http://localhost:8081');
  }

  async getCompleteProfile(username: string) {
    try {
      // Get account info from Auth Service
      const accountResponse = await firstValueFrom(
        this.httpService.get(`${this.authServiceUrl}/api/v1/accounts/${username}`)
      );
      const accountData = accountResponse.data;

      // Get user profile from User Service
      const userResponse = await firstValueFrom(
        this.httpService.get(`${this.userServiceUrl}/api/v1/users/account/${accountData.id}`)
      );
      const userData = userResponse.data;

      // Aggregate the data
      return {
        account: {
          id: accountData.id,
          username: accountData.username,
          email: accountData.email,
          emailVerified: accountData.emailVerified,
          accountEnabled: accountData.accountEnabled,
          lastLoginAt: accountData.lastLoginAt,
          createdAt: accountData.createdAt,
        },
        profile: {
          id: userData.id,
          firstName: userData.firstName,
          lastName: userData.lastName,
          phone: userData.phone,
          profilePictureUrl: userData.profilePictureUrl,
          locale: userData.locale,
          timezone: userData.timezone,
          termsAcceptedAt: userData.termsAcceptedAt,
          privacyPolicyAcceptedAt: userData.privacyPolicyAcceptedAt,
          createdAt: userData.createdAt,
          updatedAt: userData.updatedAt,
        }
      };
    } catch (error) {
      throw new Error(`Failed to fetch complete profile: ${error.message}`);
    }
  }
}