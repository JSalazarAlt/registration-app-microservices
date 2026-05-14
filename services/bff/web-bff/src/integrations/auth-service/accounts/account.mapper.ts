import { LoginDTO } from '../../modules/auth/dto/login.dto';
import { RegistrationDTO } from '../../modules/auth/dto/registration.dto';

import {
    AuthenticationRequest,
    RegistrationRequest,
    AuthenticationResponse,
} from './account.types';

export class AccountMapper {

    // ----------------------------------------------------------------
    // BFF REQUESTS → AUTH MICROSERVICE REQUESTS
    // ----------------------------------------------------------------

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