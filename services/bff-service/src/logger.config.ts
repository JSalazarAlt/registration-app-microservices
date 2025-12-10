import * as winston from 'winston';
import { trace } from '@opentelemetry/api';

const LogstashTransport = require('winston-logstash/lib/winston-logstash-latest');

const logstashTransport = new LogstashTransport({
    port: 5000,
    host: process.env.LOGSTASH_HOST || 'localhost',
    node_name: 'bff-service',
    max_connect_retries: 3,
});

logstashTransport.on('error', (err) => {
    console.error('Logstash transport error:', err.message);
});

logstashTransport.on('connect', () => {
    console.log('Connected to Logstash on port 5000');
});

const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(
        winston.format.timestamp({ format: 'YYYY-MM-DDTHH:mm:ss.SSSZ' }),
        winston.format((info) => {
            const span = trace.getActiveSpan();
            if (span) {
                const spanContext = span.spanContext();
                info.trace_id = spanContext.traceId;
                info.span_id = spanContext.spanId;
                info.trace_flags = spanContext.traceFlags.toString(16).padStart(2, '0');
            }
            info.service = 'bff-service';
            return info;
        })(),
        winston.format.json(),
    ),
    transports: [
        new winston.transports.Console(),
        logstashTransport,
    ],
    exceptionHandlers: [
        new winston.transports.Console(),
    ],
});

export default logger;