import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { ProfileController } from './profile.controller';
import { ProfileService } from './profile.service';

@Module({
  imports: [HttpModule],
  controllers: [ProfileController],
  providers: [ProfileService],
})
export class ProfileModule {}