import { Controller, Post, Get, Body, Param, HttpCode, Req, Res, UseGuards, UnauthorizedException, Logger } from '@nestjs/common';
import type { Response } from 'express';
import { AuthService } from './auth.service';
import { LoginDTO } from './dto/login.dto';
import { RefreshTokenDTO } from './dto/refresh-token.dto';
import { RegistrationDTO } from './dto/registration.dto';
import { JwtAuthGuard } from './guard/jwt-auth.guards';
import type { Request } from 'express';

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

    @Post('login/web')
    @HttpCode(200)
    async webLogin(@Body() loginData: LoginDTO, @Res({ passthrough: true }) res: Response) {
        const result = await this.authService.webLogin(loginData);
        if (result?.headers?.['set-cookie']) {
            res.setHeader('Set-Cookie', result.headers['set-cookie']);
        }
        return result.data;
    }

    @Post('login/mobile')
    @HttpCode(200)
    async mobileLogin(@Body() loginData: LoginDTO) {
        return this.authService.mobileLogin(loginData);
    }

    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 REGISTRATION AND LOGIN
    // ----------------------------------------------------------------



    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    @UseGuards(JwtAuthGuard)
    @Post('logout/web')
    @HttpCode(204)
    async logout(@Req() req: Request) {
        const token = req.headers['authorization'];
        const refreshToken = req.cookies?.refreshToken;
        
        if (!refreshToken) {
            throw new UnauthorizedException('Refresh token cookie missing');
        }

        return this.authService.logout(token!, refreshToken);
    }

    @UseGuards(JwtAuthGuard)
    @Post('global-logout/web')
    @HttpCode(204)
    async globalLogout(@Req() req: Request) {
        const token = req.headers['authorization'];
        const refreshToken = req.cookies?.refreshToken;

        if (!refreshToken) {
            throw new UnauthorizedException('Refresh token cookie missing');
        }

        return this.authService.globalLogout(token!, refreshToken);
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    @Post('refresh/web')
    @HttpCode(200)
    async webRefreshToken(@Req() req: Request, @Res({ passthrough: true }) res: Response) {
        const refreshToken = req.cookies?.refreshToken;
        
        if (!refreshToken) {
            throw new UnauthorizedException('Refresh token cookie missing');
        }

        const result = await this.authService.webRefreshToken(refreshToken);
        
        if (result?.headers?.['set-cookie']) {
            res.setHeader('Set-Cookie', result.headers['set-cookie']);
        }
        return result.data;
    }

    @Post('refresh/mobile')
    @HttpCode(200)
    async mobileRefreshToken(@Body() refreshData: RefreshTokenDTO) {
        return this.authService.mobileRefreshToken(refreshData);
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

}