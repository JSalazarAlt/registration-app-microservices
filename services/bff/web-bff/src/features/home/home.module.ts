import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { JwtModule } from '@nestjs/jwt';
import { HomeController } from './home.controller';
import { HomeService } from './home.service';
import { JwtAuthGuard } from '../../security/auth/jwt-auth.guards';
import * as fs from 'fs';

/**
 * Module for home view.
 *
 * BFF module backing the Home view. Exposes authenticated endpoints that
 * aggregate data for the main UI.
 */
@Module({
    imports: [
        // HTTP client for calling downstream services and JWT verification
        HttpModule,
        JwtModule.register({
            publicKey: fs.readFileSync('keys/public.pem'),
            signOptions: { algorithm: 'RS256' },
        }),
    ],
    
    // UI-facing endpoints for home view
    controllers: [HomeController],

    // Orchestration logic and auth guard
    providers: [HomeService, JwtAuthGuard],
})
export class HomeModule {}