import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class RegistrationDTO {

    // ----------------------------------------------------------------
    // ACCOUNT'S INFORMATION
    // ----------------------------------------------------------------

    @IsString()
    @IsNotEmpty()
    username!: string;

    @IsEmail()
    email!: string;

    @IsString()
    password!: string;

    // ----------------------------------------------------------------
    // USER'S PROFILE
    // ----------------------------------------------------------------

    firstName!: string;
    lastName!: string;
    phone!: string;
    profilePictureUrl!: string;
    locale!: string;
    timezone!: string;

}