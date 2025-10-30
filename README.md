# Suyos Platform

## Structure
- /services/authservice → Authentication & token handling
- /services/userservice → User profile and data
- /services/bffservice → Backend-for-Frontend (aggregates data)
- /web/frontend → React app
- /infra → Docker, CI/CD configs

## Running locally
1. Start microservices: `docker-compose up`
2. Start BFF: `npm run start:dev`
3. Start frontend: `npm run dev` (from /web/frontend)
