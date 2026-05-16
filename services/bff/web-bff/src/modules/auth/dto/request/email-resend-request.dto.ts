import { IsEmail } from 'class-validator';

export class EmailResendRequestDTO {

    @IsEmail()
    email!: string;

}