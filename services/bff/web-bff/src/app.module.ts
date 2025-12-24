import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './modules/auth/auth.module';
import { UserModule } from './modules/user/user.module';
import { HomeModule } from './modules/home/home.module';
import { ProfileModule } from './modules/profile/profile.module';
import { SessionModule } from './modules/sessions/session.module';

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