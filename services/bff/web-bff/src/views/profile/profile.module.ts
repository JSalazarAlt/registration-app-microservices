import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { JwtModule } from '@nestjs/jwt';
import { ProfileController } from './profile.controller';
import { ProfileService } from './profile.service';
import { JwtAuthGuard } from '../../auth/guard/jwt-auth.guards';
import * as fs from 'fs';

/**
 * Module for profile view.
 *
 * BFF module backing the authenticated user profile view. Aggregates and
 * shapes user-specific data for profile rendering.
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
    
    // UI-facing routes for profile view
    controllers: [ProfileController],

    // Orchestration logic and auth guard
    providers: [ProfileService, JwtAuthGuard],
})
export class ProfileModule {}