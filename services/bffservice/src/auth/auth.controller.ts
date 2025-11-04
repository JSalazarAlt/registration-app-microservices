import { Controller, Post, Get, Body, Param } from '@nestjs/common';
import { AuthService } from './auth.service';

@Controller('api/auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('register')
  async register(@Body() registerData: any) {
    return this.authService.register(registerData);
  }

  @Post('login')
  async login(@Body() loginData: any) {
    return this.authService.login(loginData);
  }

  @Post('logout')
  async logout(@Body() logoutData: any) {
    return this.authService.logout(logoutData);
  }

  @Post('refresh')
  async refreshToken(@Body() refreshData: any) {
    return this.authService.refreshToken(refreshData);
  }

  @Get('accounts/:username')
  async getAccountByUsername(@Param('username') username: string) {
    return this.authService.getAccountByUsername(username);
  }
}