import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class LoginRequestDTO {
    
    @IsString()
    @IsNotEmpty()
    identifier!: string;

    @IsString()
    password!: string;

}