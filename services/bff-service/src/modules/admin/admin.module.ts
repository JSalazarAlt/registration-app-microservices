import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { AuthService } from '../auth/auth.service';
import { UserService } from '../user/user.service';
import { UserController } from './user.controller';
import { AccountController } from './account.controller';

@Module({
    imports: [HttpModule],
    controllers: [UserController, AccountController],
    providers: [UserService, AuthService],
})
export class UserModule {}