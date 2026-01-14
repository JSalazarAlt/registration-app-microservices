import { Controller, Get, Put, Body, Param, Query } from '@nestjs/common';
import { UserService } from '../../user/user.service';
import { UserUpdateDTO } from '../../user/dto/user-update.dto';

@Controller('api/users')
export class UserController {

    constructor(private readonly userService: UserService) {}
    
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

    @Get(':userId')
    async getUserById(@Param('userId') userId: string) {
        return this.userService.getUserById(userId);
    }

    @Put(':userId')
    async updateUserById(
        @Param('userId') userId: string,
        @Body() updateData: UserUpdateDTO
    ) {
        return this.userService.updateUserById(userId, updateData);
    }
    
}