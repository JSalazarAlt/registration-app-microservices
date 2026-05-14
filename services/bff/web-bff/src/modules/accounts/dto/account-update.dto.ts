import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class AccountUpdateRequest {

    // ----------------------------------------------------------------
    // CREDENTIALS
    // ----------------------------------------------------------------

    username!: string;

    email!: string;

}