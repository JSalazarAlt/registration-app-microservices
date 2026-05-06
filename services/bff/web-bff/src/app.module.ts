import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './modules/auth/auth.module';
import { UserModule } from './features/user/user.module';
import { HomeModule } from './bff/home/home.module';
import { ProfileModule } from './bff/profile/profile.module';
import { SessionModule } from './features/sessions/session.module';

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
    controllers: [AppController], // controllers: [AppController],
    providers: [AppService], // providers: [AppService],
})
export class AppModule {}