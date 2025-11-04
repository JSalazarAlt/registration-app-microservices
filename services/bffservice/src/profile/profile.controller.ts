import { Controller, Get, Param } from '@nestjs/common';
import { ProfileService } from './profile.service';

@Controller('api/profile')
export class ProfileController {
  constructor(private readonly profileService: ProfileService) {}

  @Get(':username')
  async getCompleteProfile(@Param('username') username: string) {
    return this.profileService.getCompleteProfile(username);
  }
}