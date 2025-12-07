import '../otel/otel';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { WinstonLoggerService } from './winston-logger.service';

async function bootstrap() {
    const app = await NestFactory.create(AppModule, {
        logger: new WinstonLoggerService(),
    });
    
    app.enableCors({
        origin: ['http://localhost:5173', 'http://localhost:3001'],
        methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
        allowedHeaders: ['Content-Type', 'Authorization'],
        credentials: true,
    });
    
    const port = process.env.PORT || 3001;
    await app.listen(port);
    console.log(`\n🚀 BFF Service is running on http://localhost:${port}\n`);
}

bootstrap();
