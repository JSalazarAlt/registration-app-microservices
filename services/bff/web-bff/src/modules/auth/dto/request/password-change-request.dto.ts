import { IsString } from 'class-validator';

export class PasswordChangeRequestDTO {

    @IsString()
    currentPassword!: string;

    @IsString()
    newPassword!: string;

}