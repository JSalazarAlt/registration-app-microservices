import { Controller, Post, Get, Body, Param, HttpCode, Req, Res, UseGuards, UnauthorizedException, Logger } from '@nestjs/common';
import type { Response } from 'express';
import { AuthService } from './auth.service';
import { LoginDTO } from './dto/login.dto';
import { RegistrationDTO } from './dto/registration.dto';
import { JwtAuthGuard } from '../../security/auth/jwt-auth.guards';
import type { Request } from 'express';
import { EmailVerificationTokenDTO } from './dto/email-verification-token.dto';

/**
 * Controller handling authentication-related endpoints.
 *
 * Provides REST endpoints for account registration, login, logout, and token
 * refresh. Delegates the business logic to the AuthService, acting as entry
 * point for requests to the authentication subsystem.
 */
@Controller('api/v1/auth')
export class AuthController {

    private readonly REFRESH_TOKEN_LIFETIME_DAYS = 30;
    
    constructor(private readonly authService: AuthService) {}

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------
    
    @Post('register')
    @HttpCode(201)
    async register(@Body() registerData: RegistrationDTO) {
        return this.authService.register(registerData);
    }

    /**
     * Handles login by forwarding credentials to the Auth microservice.
     *
     * Forwards the login credentials to the Auth microservice, receives
     * issued refresh and access tokens, stores the refresh token in an
     * HTTP-only cookie, and returns the access token to the client.
     *
     * @param loginData Login credentials (username/email and password)
     * @param res HTTP response used to set refresh token cookie
     * @returns Access token information and account identifier
     * @throws HttpException if authentication fails or Auth microservice is
     * unavailable
     */
    @Post('login')
    @HttpCode(200)
    async login(@Body() loginData: LoginDTO, @Res({ passthrough: true }) res: Response) {
        // Perform login via AuthService
        const response = await this.authService.login(loginData);
        
        // Extract refresh token from response
        const refreshToken = response.refreshToken;

        // Store refresh token in cookie
        res.cookie('refreshToken', refreshToken, {
            httpOnly: true,
            secure: true,
            sameSite: 'lax',
            maxAge: this.REFRESH_TOKEN_LIFETIME_DAYS * 24 * 60 * 60 * 1000, // example 30 days
        });

        // Return access token
        return {
            accountId: response.accountId,
            accessToken: response.accessToken,
            tokenType: response.tokenType,
            accessTokenExpiresIn: response.accessTokenExpiresIn
        };
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
    async logout(@Req() req: Request) {
        const token = req.headers['authorization'];
        const refreshToken = req.cookies?.refreshToken;
        
        if (!refreshToken) {
            throw new UnauthorizedException('Refresh token cookie missing');
        }

        return this.authService.logout(token!, refreshToken);
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    /**
     * Handles access token refresh by forwarding refresh token to the Auth
     * microservice.
     *
     * Forwards the current refresh token to the Auth microservice, receives
     * newly issued refresh and access tokens, stores the refresh token in an
     * HTTP-only cookie, and returns the access token to the client.
     *
     * @param req HTTP request with cookie containing current refresh token
     * @param res HTTP response used to set rotated refresh token cookie
     * @returns Access token information and account identifier
     * @throws HttpException if authentication fails or Auth microservice is
     * unavailable
     */
    @Post('refresh')
    @HttpCode(200)
    async refreshToken(@Req() req: Request, @Res({ passthrough: true }) res: Response) {
        // Extract current refresh token from cookies
        const refreshToken = req.cookies?.refreshToken;
        
        // Perform refresh token via AuthService
        const response = await this.authService.refreshToken(refreshToken);
        
        // Extract rotated refresh token from response
        const rotatedRefreshToken = response.refreshToken;

        // Set refresh token cookie
        res.cookie('refreshToken', rotatedRefreshToken, {
            httpOnly: true,
            secure: true,
            sameSite: 'lax',
            maxAge: this.REFRESH_TOKEN_LIFETIME_DAYS * 24 * 60 * 60 * 1000,
        });

        // Return access token
        return {
            accountId: response.accountId,
            accessToken: response.accessToken,
            tokenType: response.tokenType,
            accessTokenExpiresIn: response.accessTokenExpiresIn
        };
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    @Post('email/verify')
    @HttpCode(200)
    async verifyEmail(@Body() emailVerificationTokenDTO: EmailVerificationTokenDTO) {
        // Extract current refresh token from cookies
        const refreshToken = emailVerificationTokenDTO.value;

        // Return updated account's information
        return await this.authService.verifyEmail(refreshToken);
    }

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

}