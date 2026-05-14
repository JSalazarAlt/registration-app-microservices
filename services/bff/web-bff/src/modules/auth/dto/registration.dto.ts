import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class RegistrationDTO {

    // ----------------------------------------------------------------
    // ACCOUNT'S CREDENTIALS
    // ----------------------------------------------------------------

    username!: string;

    email!: string;

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