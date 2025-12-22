import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { JwtModule } from '@nestjs/jwt';
import { HomeController } from './home.controller';
import { HomeService } from './home.service';
import { JwtAuthGuard } from '../auth/guard/jwt-auth.guards';
import * as fs from 'fs';

/**
 * Module for user functionality.
 *
 * Imports HttpModule for HTTP communication. Registers AuthController
 * and provides AuthService for login, registration, logout, and token
 * refresh operations.
 */
@Module({
    imports: [  HttpModule, 
                JwtModule.register({publicKey: fs.readFileSync('keys/public.pem'),
                                    signOptions: { algorithm: 'RS256' },})
             ],
    controllers: [HomeController],
    providers: [HomeService, JwtAuthGuard],
})
export class HomeModule {}