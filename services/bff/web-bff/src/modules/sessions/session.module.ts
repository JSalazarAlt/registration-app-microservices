import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { JwtModule } from '@nestjs/jwt';
import { SessionController } from './session.controller';
import { SessionService } from './session.service';
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
    controllers: [SessionController],
    providers: [SessionService, JwtAuthGuard],
})
export class SessionModule {}