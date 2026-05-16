import { IsString } from 'class-validator';

export class RefreshTokenRequestDTO {

    @IsString()
    value!: string;

}