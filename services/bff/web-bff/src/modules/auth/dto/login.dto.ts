import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class LoginDTO {
    
    @IsString()
    @IsNotEmpty()
    identifier!: string;

    @IsString()
    password!: string;

}