import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './auth/auth.module';
import { UserModule } from './user/user.module';
import { HomeModule } from './views/home/home.module';
import { ProfileModule } from './views/profile/profile.module';
import { SessionModule } from './views/sessions/session.module';

@Module({
    imports: [
        ConfigModule.forRoot({ isGlobal: true }),
        HttpModule,
        AuthModule,
        UserModule,
        HomeModule,
        ProfileModule,
        SessionModule
    ],
    controllers: [AppController],
    providers: [AppService],
})
export class AppModule {}