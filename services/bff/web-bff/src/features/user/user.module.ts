import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { UserController } from './user.controller';
import { UserService } from './user.service';
import { JwtModule } from '@nestjs/jwt';
import { JwtAuthGuard } from '../../security/auth/jwt-auth.guards';
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
    controllers: [UserController],
    providers: [UserService, JwtAuthGuard],
})
export class UserModule {}