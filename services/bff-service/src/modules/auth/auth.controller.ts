import { Controller, Post, Get, Body, Param, HttpCode } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDTO } from './dto/login.dto';
import { RefreshTokenDTO } from './dto/refresh-token.dto';
import { RegistrationDTO } from './dto/registration.dto';

/**
 * Controller handling authentication-related endpoints.
 *
 * Provides REST endpoints for account registration, login, logout, and token
 * refresh. Delegates the business logic to the AuthService, acting as entry
 * point for requests to the authentication subsystem.
 */
@Controller('api/auth')
export class AuthController {
    
    constructor(private readonly authService: AuthService) {}

    @Post('register')
    async register(@Body() registerData: RegistrationDTO) {
        return this.authService.register(registerData);
    }

    @Post('login')
    @HttpCode(200)
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