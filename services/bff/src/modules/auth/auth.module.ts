import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';

/**
 * Module for authentication functionality.
 *
 * Imports HttpModule for HTTP communication. Registers AuthController
 * and provides AuthService for login, registration, logout, and token
 * refresh operations.
 */
@Module({
    imports: [HttpModule],
    controllers: [AuthController],
    providers: [AuthService],
})
export class AuthModule {}