import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { JwtModule } from '@nestjs/jwt';
import { SessionController } from './session.controller';
import { SessionService } from './session.service';
import { JwtAuthGuard } from '../../auth/guard/jwt-auth.guards';
import * as fs from 'fs';

/**
 * Module for sessions view.
 *
 * BFF module backing the Sessions view. Exposes authenticated endpoints that
 * aggregate data for the main UI.
 */
@Module({
    // HTTP client for calling downstream services and JWT verification
    imports: [  HttpModule, 
                JwtModule.register({publicKey: fs.readFileSync('keys/public.pem'),
                                    signOptions: { algorithm: 'RS256' },})
             ],

    // UI-facing endpoints for sessions view
    controllers: [SessionController],

    // Orchestration logic and auth guard
    providers: [SessionService, JwtAuthGuard],
})
export class SessionModule {}