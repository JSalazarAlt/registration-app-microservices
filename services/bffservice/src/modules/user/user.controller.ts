import { Controller, Get, Put, Body, Param, Query } from '@nestjs/common';
import { UserService } from './user.service';

@Controller('api/users')
export class UserController {

    constructor(private readonly userService: UserService) {}

    @Get(':userId')
    async getUserById(@Param('userId') userId: string) {
        return this.userService.getUserById(userId);
    }

    @Put(':userId')
    async updateUserById(
        @Param('userId') userId: string,
        @Body() updateData: any
    ) {
        return this.userService.updateUserById(userId, updateData);
    }

    @Get('account/:accountId')
    async getUserByAccountId(@Param('accountId') accountId: string) {
        return this.userService.getUserByAccountId(accountId);
    }

    @Put('account/:accountId')
    async updateUserByAccountId(
        @Param('accountId') accountId: string,
        @Body() updateData: any
    ) {
        return this.userService.updateUserByAccountId(accountId, updateData);
    }

    @Get()
    async getAllUsers(
        @Query('page') page = 0,
        @Query('size') size = 10,
        @Query('sortBy') sortBy = 'createdAt',
        @Query('sortDir') sortDir = 'desc'
    ) {
        return this.userService.getAllUsers(page, size, sortBy, sortDir);
    }

    @Get('search')
    async searchUsers(@Query('name') name: string) {
        return this.userService.searchUsers(name);
    }
    
}