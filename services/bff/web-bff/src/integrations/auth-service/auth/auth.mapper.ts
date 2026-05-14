import { RegistrationDTO } from '../../../modules/auth/dto/registration.dto';
import { LoginDTO } from '../../../modules/auth/dto/login.dto';
import { OAuth2LoginDTO } from '../../../modules/auth/dto/oauth2-login.dto';

import {
    RegistrationRequest,
    AuthenticationRequest,
    OAuth2AuthenticationRequest,
    AuthenticationResponse
} from './auth.types';

export class AuthMapper {

    // ----------------------------------------------------------------
    // BFF REQUESTS → AUTH MICROSERVICE REQUESTS
    // ----------------------------------------------------------------

    static toAuthenticationRequest(dto: LoginDTO): AuthenticationRequest {
        return {
            identifier: dto.identifier,
            password: dto.password,
        };
    }

    static toRegistrationRequest(dto: RegistrationDTO): RegistrationRequest {
        return {
            username: dto.username,
            email: dto.email,
            password: dto.password,
            firstName: dto.firstName,
            lastName: dto.lastName,
            phoneNumber: dto.phone,
            profilePictureUrl: dto.profilePictureUrl,
            locale: dto.locale,
            timezone: dto.timezone
        };
    }

    static toOAuth2AuthenticationRequest(dto: OAuth2LoginDTO): OAuth2AuthenticationRequest {
        return {
            email: dto.email,
            name: dto.name,
            provider: dto.provider,
            providerId: dto.providerId,
            deviceName: dto.deviceName,
        }

    }

    static toRefreshRequest(refreshToken: string) {
        return {
            value: refreshToken,
        };
    }

    static toVerifyEmailRequest(token: string) {
        return {
            value: token,
        };
    }

    // ----------------------------------------------------------------
    // AUTH MICROSERVICE RESPONSES → BFF RESPONSES
    // ----------------------------------------------------------------

    static toAuthTokensResponse(data: any): AuthenticationResponse {
        return {
            accountId: data.accountId,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            tokenType: data.tokenType,
            accessTokenExpiresIn: data.accessTokenExpiresIn,
        };
    }

}