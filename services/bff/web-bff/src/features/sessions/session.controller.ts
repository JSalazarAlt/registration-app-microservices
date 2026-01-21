import { Controller, Get, Put, Body, UseGuards, Req, Delete, HttpCode } from '@nestjs/common';
import { JwtAuthGuard } from '../../security/auth/jwt-auth.guards';
import { SessionService } from './session.service';

@Controller('api/v1/sessions')
export class SessionController {

    constructor(private readonly sessionService: SessionService) {}

    /** Retrieves the authenticated account's all active sessions. */
    @UseGuards(JwtAuthGuard)
    @Get('me')
    @HttpCode(200)
    async getMySessions(@Req() req: any) {
        const accountId = req.accountId;
        const token = req.headers['authorization'];
        return this.sessionService.getAuthenticatedAccountSessions(accountId, token);
    }

    /** Terminates the authenticated account's all active sessions. */
    @UseGuards(JwtAuthGuard)
    @Delete('me')
    @HttpCode(204)
    async terminateMySessions(@Req() req: any) {
        const accountId = req.accountId;
        const token = req.headers['authorization'];
        return this.sessionService.terminateAuthenticatedAccountSessions(accountId, token);
    }

}