import '../otel/otel';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { WinstonLoggerService } from './common/logger/winston-logger.service';
import cookieParser from 'cookie-parser';

async function bootstrap() {
    const app = await NestFactory.create(AppModule, {
        logger: new WinstonLoggerService(),
    });

    app.use(cookieParser());
    
    const port = process.env.PORT || 3001;
    await app.listen(port);
    console.log(`\n🚀 BFF Service is running on http://localhost:${port}\n`);
}

bootstrap();