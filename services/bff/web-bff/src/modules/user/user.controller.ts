import { Controller, Get, Put, Body, UseGuards, Req } from '@nestjs/common';
import { UserService } from './user.service';
import { JwtAuthGuard } from '../auth/guard/jwt-auth.guards';
import { UserUpdateDTO } from './dto/user-update.dto';

@Controller('api/v1/users')
export class UserController {

    constructor(private readonly userService: UserService) {}

    @UseGuards(JwtAuthGuard)
    @Get('me')
    async getAuthenticatedUser(@Req() req: Request) {
        const token = req.headers['authorization'];
        return this.userService.getAuthenticatedUser(token);
    }

    @UseGuards(JwtAuthGuard)
    @Put('me')
    async updateAuthenticatedUser(
        @Req() req: Request,
        @Body() updateData: UserUpdateDTO
    ) {
        const token = req.headers['authorization'];
        return this.userService.updateAuthenticatedUser(token, updateData);
    }
    
}