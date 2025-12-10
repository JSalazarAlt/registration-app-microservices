import { Test, TestingModule } from '@nestjs/testing';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { AuthService } from './auth.service';
import { HttpException, HttpStatus } from '@nestjs/common';
import { of, throwError } from 'rxjs';
import { AxiosError, AxiosResponse } from 'axios';
import { RegistrationDTO } from './dto/registration.dto';
import { RefreshTokenDTO } from './dto/refresh-token.dto';

describe('AuthService', () => {

    let service: AuthService;
    let httpService: HttpService;
    let configService: ConfigService;

    const mockHttpService = {
        post: jest.fn(),
        get: jest.fn(),
    };

    const mockConfigService = {
        get: jest.fn((key: string, defaultValue: any) => {
            const config = {
                AUTH_SERVICE_URL: 'http://localhost:8080',
                REQUEST_TIMEOUT: 5000,
                MAX_RETRIES: 3,
            };
            return config[key] || defaultValue;
        }),
    };

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            providers: [
                AuthService,
                { provide: HttpService, useValue: mockHttpService },
                { provide: ConfigService, useValue: mockConfigService },
            ],
        }).compile();

        service = module.get<AuthService>(AuthService);
        httpService = module.get<HttpService>(HttpService);
        configService = module.get<ConfigService>(ConfigService);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it('should be defined', () => {
        expect(service).toBeDefined();
    });

    describe('register', () => {
        it('Should successfully register a new account', async () => {
            const registerData: RegistrationDTO = {
                username: 'testuser',
                email: 'test@example.com',
                password: 'Password123!',
                firstName: 'Test',
                lastName: 'User',
                phone: '+51991234561',
                profilePictureUrl: 'asdsa',
                locale: 'en',
                timezone: 'UTC'
            };

            const mockResponse: AxiosResponse = {
                data: { id: '1', username: 'testuser', email: 'test@example.com' },
                status: 201,
                statusText: 'Created',
                headers: {},
                config: {} as any,
            };

            mockHttpService.post.mockReturnValue(of(mockResponse));

            const result = await service.register(registerData);

            expect(result).toEqual(mockResponse.data);
            expect(mockHttpService.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/v1/auth/register',
                registerData,
            );
        });

        it('Should throw HttpException when registration fails', async () => {
            const registerData: RegistrationDTO = {
                username: 'testuser',
                email: 'test@example.com',
                password: 'Password123!',
                firstName: 'Test',
                lastName: 'User',
                phone: '+51991234561',
                profilePictureUrl: 'asdsa',
                locale: 'en',
                timezone: 'UTC'
            };

            const axiosError: Partial<AxiosError> = {
                response: {
                    status: 400,
                    data: { message: 'Username already taken' },
                } as any,
                isAxiosError: true,
            };

            mockHttpService.post.mockReturnValue(throwError(() => axiosError));

            await expect(service.register(registerData)).rejects.toThrow(HttpException);
        });
    });

    describe('login', () => {
        it('Should successfully authenticate account', async () => {
            const loginData = {
                identifier: 'testuser',
                password: 'Password123!',
            };

            const mockResponse: AxiosResponse = {
                data: {
                    refreshToken: 'refresh-token',
                    accessToken: 'access-token',
                },
                status: 200,
                statusText: 'OK',
                headers: {},
                config: {} as any,
            };

            mockHttpService.post.mockReturnValue(of(mockResponse));

            const result = await service.login(loginData);

            expect(result).toEqual(mockResponse.data);
            expect(mockHttpService.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/v1/auth/login',
                loginData,
            );
        });

        it('Should throw HttpException when credentials are invalid', async () => {
            const loginData = {
                identifier: 'testuser',
                password: 'WrongPassword',
            };

            const axiosError: Partial<AxiosError> = {
                response: {
                    status: 401,
                    data: { message: 'Invalid credentials' },
                } as any,
                isAxiosError: true,
            };

            mockHttpService.post.mockReturnValue(throwError(() => axiosError));

            await expect(service.login(loginData)).rejects.toThrow(HttpException);
        });
    });

    describe('logout', () => {
        it('Should successfully logout account', async () => {
            const logoutData : RefreshTokenDTO = { value: 'refresh-token' };

            const mockResponse: AxiosResponse = {
                data: { message: 'Logout successful' },
                status: 200,
                statusText: 'OK',
                headers: {},
                config: {} as any,
            };

            mockHttpService.post.mockReturnValue(of(mockResponse));

            const result = await service.logout(logoutData);

            expect(result).toEqual(mockResponse.data);
            expect(mockHttpService.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/v1/auth/logout',
                logoutData,
            );
        });
    });

    describe('refreshToken', () => {
        it('Should successfully refresh access token', async () => {
            const refreshData = { value: 'refresh-token' };

            const mockResponse: AxiosResponse = {
                data: { accessToken: 'new-access-token' },
                status: 200,
                statusText: 'OK',
                headers: {},
                config: {} as any,
            };

            mockHttpService.post.mockReturnValue(of(mockResponse));

            const result = await service.refreshToken(refreshData);

            expect(result).toEqual(mockResponse.data);
            expect(mockHttpService.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/v1/auth/refresh',
                refreshData,
            );
        });
    });

    describe('getAccountByUsername', () => {
        it('Should successfully retrieve account by username', async () => {
            const username = 'testuser';

            const mockResponse: AxiosResponse = {
                data: {
                    id: '1',
                    username: 'testuser',
                    email: 'test@example.com',
                    emailVerified: true,
                },
                status: 200,
                statusText: 'OK',
                headers: {},
                config: {} as any,
            };

            mockHttpService.get.mockReturnValue(of(mockResponse));

            const result = await service.getAccountByUsername(username);

            expect(result).toEqual(mockResponse.data);
            expect(mockHttpService.get).toHaveBeenCalledWith(
                'http://localhost:8080/api/v1/accounts/testuser',
            );
        });

        it('Should throw HttpException when account not found', async () => {
            const username = 'nonexistent';

            const axiosError: Partial<AxiosError> = {
                response: {
                    status: 404,
                    data: { message: 'Account not found' },
                } as any,
                isAxiosError: true,
            };

            mockHttpService.get.mockReturnValue(throwError(() => axiosError));

            await expect(service.getAccountByUsername(username)).rejects.toThrow(HttpException);
        });
    });

    describe('handleServiceError', () => {
        it('should handle service unavailable error', async () => {
            const loginData = { identifier: 'testuser', password: 'Password123!' };

            const axiosError: Partial<AxiosError> = {
                code: 'ECONNREFUSED',
                isAxiosError: true,
            };

            mockHttpService.post.mockReturnValue(throwError(() => axiosError));

            await expect(service.login(loginData)).rejects.toThrow(
                new HttpException('Auth service is currently unavailable', HttpStatus.SERVICE_UNAVAILABLE),
            );
        });

        it('should handle timeout error', async () => {
            const loginData = { identifier: 'testuser', password: 'Password123!' };

            const timeoutError: any = {
                name: 'TimeoutError',
                message: 'Timeout',
            };

            mockHttpService.post.mockReturnValue(throwError(() => timeoutError));

            await expect(service.login(loginData)).rejects.toThrow(
                new HttpException('Auth service request timed out', HttpStatus.GATEWAY_TIMEOUT),
            );
        });
    });

});