import { Controller, Get, Put, Body, UseGuards, Req } from '@nestjs/common';
import { HomeService } from './home.service';
import { JwtAuthGuard } from '../../security/auth/jwt-auth.guards';

@Controller('api/v1/home')
export class HomeController {

    constructor(private readonly homeService: HomeService) {}

    @UseGuards(JwtAuthGuard)
    @Get()
    async getHome(@Req() req: Request) {
        const token = req.headers['authorization'];
        return this.homeService.getHomeData(token);
    }

}