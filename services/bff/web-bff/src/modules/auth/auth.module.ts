import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { JwtAuthGuard } from './guard/jwt-auth.guards';
import { JwtModule } from '@nestjs/jwt';
import * as fs from 'fs';

/**
 * Module for authentication functionality.
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
    controllers: [AuthController],
    providers: [AuthService, JwtAuthGuard],
})
export class AuthModule {}