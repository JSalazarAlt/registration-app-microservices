import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class OAuth2RequestDTO {
    
    @IsEmail()
    email!: string;

    @IsString()
    name!: string;

    provider!: string;

    providerId!: string;

    deviceName!: string;

}