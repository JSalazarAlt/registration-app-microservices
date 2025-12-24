import { Controller, Get, Put, Body, UseGuards, Req } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guard/jwt-auth.guards';
import { SessionService } from './session.service';

@Controller('api/v1/sessions')
export class SessionController {

    constructor(private readonly sessionService: SessionService) {}

    @UseGuards(JwtAuthGuard)
    @Get('me')
    async getAuthenticatedAccountSessions(@Req() req: Request) {
        const token = req.headers['authorization'];
        return this.sessionService.getAuthenticatedAccountSessions(token);
    }

}