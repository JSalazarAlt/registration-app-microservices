import { IsEmail } from 'class-validator';

export class PasswordForgotRequestDTO {

    @IsEmail()
    email!: string;

}