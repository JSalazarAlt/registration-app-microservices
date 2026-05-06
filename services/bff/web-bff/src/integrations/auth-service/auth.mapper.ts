// integrations/auth-service/auth.mapper.ts

import { LoginDTO } from '../../modules/auth/dto/login.dto';
import { RegistrationDTO } from '../../modules/auth/dto/registration.dto';

import {
    AuthenticationRequest,
    RegistrationRequest,
    AuthenticationResponse,
} from './auth.types';

export class AuthMapper {

    // ----------------------------------------------------------------
    // BFF REQUESTS → AUTH MICROSERVICE REQUESTS
    // ----------------------------------------------------------------

    static toLoginRequest(dto: LoginDTO): AuthenticationRequest {
        return {
            identifier: dto.identifier,
            password: dto.password,
        };
    }

    static toRegisterRequest(dto: RegistrationDTO): RegistrationRequest {
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