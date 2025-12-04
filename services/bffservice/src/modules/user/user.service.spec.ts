import { Test, TestingModule } from '@nestjs/testing';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { UserService } from './user.service';
import { HttpException, HttpStatus } from '@nestjs/common';
import { of, throwError } from 'rxjs';
import { AxiosError, AxiosResponse } from 'axios';

describe('UserService', () => {
  let service: UserService;
  let httpService: HttpService;

  const mockHttpService = {
    get: jest.fn(),
    put: jest.fn(),
  };

  const mockConfigService = {
    get: jest.fn((key: string, defaultValue: any) => {
      const config = {
        USER_SERVICE_URL: 'http://localhost:8081',
        REQUEST_TIMEOUT: 5000,
        MAX_RETRIES: 3,
      };
      return config[key] || defaultValue;
    }),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        UserService,
        { provide: HttpService, useValue: mockHttpService },
        { provide: ConfigService, useValue: mockConfigService },
      ],
    }).compile();

    service = module.get<UserService>(UserService);
    httpService = module.get<HttpService>(HttpService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('getUserProfile', () => {
    it('should successfully retrieve user profile by account ID', async () => {
      const accountId = 'account-123';

      const mockResponse: AxiosResponse = {
        data: {
          id: 'user-123',
          accountId: 'account-123',
          firstName: 'Test',
          lastName: 'User',
          email: 'test@example.com',
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      mockHttpService.get.mockReturnValue(of(mockResponse));

      const result = await service.getUserProfile(accountId);

      expect(result).toEqual(mockResponse.data);
      expect(mockHttpService.get).toHaveBeenCalledWith(
        'http://localhost:8081/api/v1/users/account/account-123',
      );
    });

    it('should throw HttpException when profile not found', async () => {
      const accountId = 'nonexistent';

      const axiosError: Partial<AxiosError> = {
        response: {
          status: 404,
          data: { message: 'User profile not found' },
        } as any,
        isAxiosError: true,
      };

      mockHttpService.get.mockReturnValue(throwError(() => axiosError));

      await expect(service.getUserProfile(accountId)).rejects.toThrow(HttpException);
    });
  });

  describe('updateUserProfile', () => {
    it('should successfully update user profile', async () => {
      const accountId = 'account-123';
      const updateData = {
        firstName: 'Updated',
        lastName: 'Name',
      };

      const mockResponse: AxiosResponse = {
        data: {
          id: 'user-123',
          accountId: 'account-123',
          firstName: 'Updated',
          lastName: 'Name',
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      mockHttpService.put.mockReturnValue(of(mockResponse));

      const result = await service.updateUserProfile(accountId, updateData);

      expect(result).toEqual(mockResponse.data);
      expect(mockHttpService.put).toHaveBeenCalledWith(
        'http://localhost:8081/api/v1/users/account/account-123',
        updateData,
      );
    });

    it('should throw HttpException when update fails', async () => {
      const accountId = 'account-123';
      const updateData = { firstName: 'Updated' };

      const axiosError: Partial<AxiosError> = {
        response: {
          status: 400,
          data: { message: 'Invalid update data' },
        } as any,
        isAxiosError: true,
      };

      mockHttpService.put.mockReturnValue(throwError(() => axiosError));

      await expect(service.updateUserProfile(accountId, updateData)).rejects.toThrow(HttpException);
    });
  });

  describe('getAllUsers', () => {
    it('should successfully retrieve paginated users', async () => {
      const mockResponse: AxiosResponse = {
        data: {
          content: [
            { id: 'user-1', firstName: 'User', lastName: 'One' },
            { id: 'user-2', firstName: 'User', lastName: 'Two' },
          ],
          page: 0,
          size: 10,
          totalElements: 2,
          totalPages: 1,
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      mockHttpService.get.mockReturnValue(of(mockResponse));

      const result = await service.getAllUsers(0, 10, 'createdAt', 'desc');

      expect(result).toEqual(mockResponse.data);
      expect(mockHttpService.get).toHaveBeenCalledWith(
        'http://localhost:8081/api/v1/users',
        { params: { page: 0, size: 10, sortBy: 'createdAt', sortDir: 'desc' } },
      );
    });
  });

  describe('searchUsers', () => {
    it('should successfully search users by name', async () => {
      const searchName = 'John';

      const mockResponse: AxiosResponse = {
        data: [
          { id: 'user-1', firstName: 'John', lastName: 'Doe' },
          { id: 'user-2', firstName: 'Johnny', lastName: 'Smith' },
        ],
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      mockHttpService.get.mockReturnValue(of(mockResponse));

      const result = await service.searchUsers(searchName);

      expect(result).toEqual(mockResponse.data);
      expect(mockHttpService.get).toHaveBeenCalledWith(
        'http://localhost:8081/api/v1/users/search',
        { params: { name: searchName } },
      );
    });
  });

  describe('handleServiceError', () => {
    it('should handle service unavailable error', async () => {
      const accountId = 'account-123';

      const axiosError: Partial<AxiosError> = {
        code: 'ECONNREFUSED',
        isAxiosError: true,
      };

      mockHttpService.get.mockReturnValue(throwError(() => axiosError));

      await expect(service.getUserProfile(accountId)).rejects.toThrow(
        new HttpException('User service is currently unavailable', HttpStatus.SERVICE_UNAVAILABLE),
      );
    });

    it('should handle timeout error', async () => {
      const accountId = 'account-123';

      const timeoutError: any = {
        name: 'TimeoutError',
        message: 'Timeout',
      };

      mockHttpService.get.mockReturnValue(throwError(() => timeoutError));

      await expect(service.getUserProfile(accountId)).rejects.toThrow(
        new HttpException('User service request timed out', HttpStatus.GATEWAY_TIMEOUT),
      );
    });
  });
});
