import { Controller, Get, Put, Body, Param, Query } from '@nestjs/common';
import { UserService } from './user.service';

@Controller('api/users')
export class UserController {
  constructor(private readonly userService: UserService) {}

  @Get('profile/:accountId')
  async getUserProfile(@Param('accountId') accountId: string) {
    return this.userService.getUserProfile(accountId);
  }

  @Put('profile/:accountId')
  async updateUserProfile(
    @Param('accountId') accountId: string,
    @Body() updateData: any
  ) {
    return this.userService.updateUserProfile(accountId, updateData);
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