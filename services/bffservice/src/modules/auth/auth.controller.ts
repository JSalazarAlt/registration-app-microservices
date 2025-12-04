import { Controller, Post, Get, Body, Param } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDTO } from './dto/login.dto';
import { RefreshTokenDTO } from './dto/refreshToken.dto';
import { RegistrationDTO } from './dto/registration.dto';

@Controller('api/auth')
export class AuthController {
    
    constructor(private readonly authService: AuthService) {}

    @Post('register')
    async register(@Body() registerData: RegistrationDTO) {
        return this.authService.register(registerData);
    }

    @Post('login')
    async login(@Body() loginData: LoginDTO) {
        return this.authService.login(loginData);
    }

    @Post('logout')
    async logout(@Body() logoutData: RefreshTokenDTO) {
        return this.authService.logout(logoutData);
    }

    @Post('refresh')
    async refreshToken(@Body() refreshData: RefreshTokenDTO) {
        return this.authService.refreshToken(refreshData);
    }

}