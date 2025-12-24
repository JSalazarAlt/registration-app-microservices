import { Controller, Post, Get, Body, Param, HttpCode, Req, UseGuards } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDTO } from './dto/login.dto';
import { RefreshTokenDTO } from './dto/refresh-token.dto';
import { RegistrationDTO } from './dto/registration.dto';
import { JwtAuthGuard } from './guard/jwt-auth.guards';

/**
 * Controller handling authentication-related endpoints.
 *
 * Provides REST endpoints for account registration, login, logout, and token
 * refresh. Delegates the business logic to the AuthService, acting as entry
 * point for requests to the authentication subsystem.
 */
@Controller('api/v1/auth')
export class AuthController {
    
    constructor(private readonly authService: AuthService) {}

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    @Post('register')
    async register(@Body() registerData: RegistrationDTO) {
        return this.authService.register(registerData);
    }

    @Post('login')
    @HttpCode(200)
    async login(@Body() loginData: LoginDTO) {
        return this.authService.login(loginData);
    }

    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 REGISTRATION AND LOGIN
    // ----------------------------------------------------------------



    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    @UseGuards(JwtAuthGuard)
    @Post('logout')
    @HttpCode(204)
    async logout(@Body() logoutData: RefreshTokenDTO, @Req() req: Request,) {
        const token = req.headers['authorization'];
        return this.authService.logout(token, logoutData);
    }

    @UseGuards(JwtAuthGuard)
    @Post('global-logout')
    @HttpCode(204)
    async globalLogout(@Body() logoutData: RefreshTokenDTO, @Req() req: Request,) {
        const token = req.headers['authorization'];
        return this.authService.globalLogout(token, logoutData);
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    @Post('refresh')
    async refreshToken(@Body() refreshData: RefreshTokenDTO) {
        return this.authService.refreshToken(refreshData);
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

}