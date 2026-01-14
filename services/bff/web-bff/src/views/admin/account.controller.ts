import { Controller, Get, Put, Body, Param, Query } from '@nestjs/common';
import { AuthService } from '../../auth/auth.service';

@Controller('api/users')
export class AccountController {

    constructor(private readonly authService: AuthService) {}

    /** 
    @Get('account/:accountId')
    async getUserByAccountId(@Param('accountId') accountId: string) {
        return this.authService.getAuthenticatedUser(accountId);
    }

    @Put('account/:accountId')
    async updateUserByAccountId(
        @Param('accountId') accountId: string,
        @Body() updateData: any
    ) {
        return this.authService.updateAuthenticatedUser(accountId, updateData);
    }
    */
}