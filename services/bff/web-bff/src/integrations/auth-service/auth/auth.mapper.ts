import { RegistrationRequestDTO } from '../../../modules/auth/dto/request/registration-request.dto';
import { LoginRequestDTO } from '../../../modules/auth/dto/request/login-request.dto';
import { OAuth2RequestDTO } from '../../../modules/auth/dto/request/oauth2-request.dto';

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

    static toRegistrationRequest(dto: RegistrationRequestDTO): RegistrationRequest {
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

    static toAuthenticationRequest(dto: LoginRequestDTO): AuthenticationRequest {
        return {
            identifier: dto.identifier,
            password: dto.password,
        };
    }

    static toOAuth2AuthenticationRequest(dto: OAuth2RequestDTO): OAuth2AuthenticationRequest {
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