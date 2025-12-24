import { Controller, Get, Put, Body, UseGuards, Req } from '@nestjs/common';
import { ProfileService } from './profile.service';
import { JwtAuthGuard } from '../auth/guard/jwt-auth.guards';

@Controller('api/v1/profile')
export class ProfileController {

    constructor(private readonly profileService: ProfileService) {}

    @UseGuards(JwtAuthGuard)
    @Get()
    async getProfile(@Req() req: Request) {
        const token = req.headers['authorization'];
        return this.profileService.getProfileData(token);
    }

}